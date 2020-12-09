package com.soraxus.prisons.cells;

import com.soraxus.prisons.util.Scheduler;
import com.soraxus.prisons.util.locks.ManagerLock;
import net.ultragrav.asyncworld.schematics.Schematic;
import net.ultragrav.serializer.GravSerializer;
import net.ultragrav.serializer.compressors.ZstdCompressor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

public class CellManager {
    public static CellManager instance;

    private final List<Cell> loadedCells = new ArrayList<>();
    private final ReentrantLock loadedCellsLock = new ReentrantLock(true);

    private final ManagerLock<UUID, Cell> ioLock = new ManagerLock<>();

    protected final ExecutorService service = Executors.newFixedThreadPool(4);

    private final File baseDir;

    private final Schematic cellSchematic;

    public CellManager(File baseDir, Schematic cellSchematic) {
        instance = this;
        baseDir.mkdirs();
        this.baseDir = baseDir;
        this.cellSchematic = cellSchematic;
        Scheduler.scheduleSyncRepeatingTaskT(this::update, 1, 1);
    }

    public void update() {
        loadedCellsLock.lock();
        try {
            loadedCells.forEach(Cell::update);
        } finally {
            loadedCellsLock.unlock();
        }
    }

    /**
     * Create a new cell
     */
    public CompletableFuture<Cell> createCell(UUID cellId) {
        CompletableFuture<Cell> future = new CompletableFuture<>();
        Cell loadedCell;
        if ((loadedCell = getLoadedCell(cellId)) != null) {
            future.complete(loadedCell);
            return future;
        }
        service.submit(() -> {

            while (true) {
                CompletableFuture<Cell> existing = ioLock.loadLock(cellId, future);
                if (existing != null) {
                    Cell c = existing.join();
                    if (c == null) //Create should never return null so this must be a load failure
                        continue; //Try to attain lock again
                    future.complete(c);
                    return;
                }
                break;
            }

            Cell loadedCell1;
            if ((loadedCell1 = getLoadedCell(cellId)) != null) {
                ioLock.loadUnlock(cellId, loadedCell1);
                return;
            }

            //Acquired io lock
            //Commence load
            try {

                long ms = System.currentTimeMillis();

                //Create
                Cell cell = new Cell(cellId);

                initCell(cell);

                loadedCellsLock.lock();
                try {
                    this.loadedCells.add(cell);
                } finally {
                    loadedCellsLock.unlock();
                }

                ioLock.loadUnlock(cellId, cell); //Completes future for us

                ms = System.currentTimeMillis() - ms;
                System.out.println("Took " + ms + "ms to create cell!");

                //May remove once main development finished
                ioLock.saveLock(cellId); //This is just to make sure that the required classes for unloading are loaded
                ioLock.saveUnlock(cellId); //Just in case I want to reload the plugin after I delete the original jar file
            } catch (Throwable e) {
                e.printStackTrace();
                ioLock.loadUnlock(cellId, null); //Completes future for us
            }
        });
        return future;
    }

    /**
     * Deserialize a cell without generating the world
     */
    public Cell deserializeCell(UUID cellId) {

        if (getLoadedCell(cellId) != null) {
            return getLoadedCell(cellId);
        }

        File file = getFile(cellId);
        if (!file.exists()) {
            return null; //file doesn't exist
        }

        ioLock.saveLock(cellId);
        try {
            if (!file.exists()) {
                return null; //file doesn't exist anymore
            }

            try {
                GravSerializer serializer = new GravSerializer(new FileInputStream(file), ZstdCompressor.instance);
                return new Cell(serializer);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        } finally {
            ioLock.saveUnlock(cellId);
        }
    }

    public CompletableFuture<Cell> loadCell(UUID cellId) {
        CompletableFuture<Cell> future = new CompletableFuture<>();
        if (getLoadedCell(cellId) != null) {
            future.complete(getLoadedCell(cellId));
            return future;
        }
        service.submit(() -> {

            File file = getFile(cellId);
            if (!file.exists()) {
                future.complete(null); //File doesn't exist
                return;
            }

            CompletableFuture<Cell> existing = ioLock.loadLock(cellId, future);
            if (existing != null) {
                future.complete(existing.join());
                return;
            }

            //Acquired io lock
            //Commence load

            if (!file.exists()) {
                ioLock.loadUnlock(cellId, null); //File doesn't exist anymore so return null
                return;
            }

            try {

                long ms = System.currentTimeMillis();

                //Deserialize
                GravSerializer serializer = new GravSerializer(new FileInputStream(file), ZstdCompressor.instance);
                Cell cell = new Cell(serializer);

                initCell(cell);

                if (!registerCell(cell))
                    throw new RuntimeException("Could not register cell");

                ioLock.loadUnlock(cellId, cell); //Completes future for us

                ms = System.currentTimeMillis() - ms;
                System.out.println("Took " + ms + "ms to load cell!");

                //May remove once main development finished
                ioLock.saveLock(cellId); //This is just to make sure that the required classes for unloading are loaded
                ioLock.saveUnlock(cellId); //Just in case I want to reload the plugin after I delete the original jar file
            } catch (Throwable e) {
                e.printStackTrace();
                ioLock.loadUnlock(cellId, null); //Completes future for us
            }
        });
        return future;
    }

    /**
     * Checks if a cell exists as a file, or is already loaded
     */
    public boolean cellExists(UUID cellId) {
        return this.getLoadedCell(cellId) != null || getFile(cellId).exists();
    }

    /**
     * Initializes a cell usually after deserialization
     */
    public void initCell(Cell cell) {
        //Generate world
        cell.generateWorld(getCellSchematic());
    }

    /**
     * Tries to register the cell as loaded in this manager, you should call cell.saveAndUnloadWorld() if this returns false
     *
     * @return true if successfully registered, false if a cell with this id is already loaded
     */
    public boolean registerCell(Cell cell) {
        loadedCellsLock.lock();
        try {
            if (getLoadedCell(cell.getId()) != null) {
                return false;
            }
            loadedCells.add(cell);
            return true;
        } finally {
            loadedCellsLock.unlock();
        }
    }

    public Runnable getSaveUnloadOp(Cell cell) {
        return () -> {
            ioLock.saveLock(cell.getId());
            try {

                long ms = System.currentTimeMillis();

                //Save / unload world
                cell.saveAndUnloadWorld();
                GravSerializer serializer = new GravSerializer();
                cell.serialize(serializer);

                //Unload
                loadedCellsLock.lock();
                try {
                    this.loadedCells.removeIf(c -> c.equals(cell));
                } finally {
                    loadedCellsLock.unlock();
                }

                File file = getFile(cell.getId());
                try {
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    serializer.writeToStream(new FileOutputStream(file), ZstdCompressor.instance);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ms = System.currentTimeMillis() - ms;
                System.out.println("Took " + ms + "ms to save and unload cell!");

            } finally {
                ioLock.saveUnlock(cell.getId());
            }
        };
    }

    public CompletableFuture<Void> saveAndUnloadCell(Cell cell) {
        if (cell == null)
            throw new IllegalArgumentException("Cell cannot be null!");
        CompletableFuture<Void> future = new CompletableFuture<>();
        service.submit(() -> {
            try {
                getSaveUnloadOp(cell).run();
            } finally {
                future.complete(null);
            }
        });
        return future;
    }

    public List<Cell> getLoadedCells() {
        loadedCellsLock.lock();
        try {
            return new ArrayList<>(this.loadedCells);
        } finally {
            loadedCellsLock.unlock();
        }
    }

    public Cell getLoadedCell(UUID cellId) {
        loadedCellsLock.lock();
        try {
            for (Cell loadedCell : loadedCells)
                if (loadedCell.getId().equals(cellId))
                    return loadedCell;
            return null;
        } finally {
            loadedCellsLock.unlock();
        }
    }

    public Schematic getCellSchematic() {
        return cellSchematic;
    }

    public File getFile(UUID cellId) {
        return new File(baseDir, cellId.toString() + ".cell");
    }

    public void deleteCell(UUID cellId) {

        ioLock.saveLock(cellId);

        try {
            //Delete file
            getFile(cellId).delete();

            //Unload
            this.loadedCellsLock.lock();
            try {
                this.loadedCells.removeIf(c -> c.getId().equals(cellId));
            } finally {
                this.loadedCellsLock.unlock();
            }
        } finally {
            ioLock.saveUnlock(cellId);
        }
    }


    /**
     * Called on disable
     */
    protected void cleanup() {
        instance = null;
        ioLock.saveAllLock();
        try {
            for (Cell loadedCell : this.getLoadedCells()) {
                this.saveAndUnloadCell(loadedCell).join();
            }
        } finally {
            ioLock.saveAllUnlock();
        }
    }
}
