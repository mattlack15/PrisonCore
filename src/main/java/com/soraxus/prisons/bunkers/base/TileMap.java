package com.soraxus.prisons.bunkers.base;

import com.soraxus.prisons.bunkers.BunkerManager;
import com.soraxus.prisons.util.list.ListUtil;
import net.ultragrav.serializer.GravSerializable;
import net.ultragrav.serializer.GravSerializer;
import net.ultragrav.utils.CuboidRegion;
import net.ultragrav.utils.IntVector2D;
import net.ultragrav.utils.IntVector3D;
import net.ultragrav.utils.Vector3D;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class TileMap implements GravSerializable {
    private final Bunker parent;
    private final Tile[][] tileMap;

    private final List<BunkerElement> elements;

    private final ReentrantLock lock = new ReentrantLock(true);

    private final AtomicBoolean deserializing = new AtomicBoolean(false);

    /**
     * Crate a new empty tile map
     *
     * @param parent Parent bunker
     */
    public TileMap(Bunker parent) {
        this.parent = parent;
        lock.lock();
        tileMap = new Tile[BunkerManager.BUNKER_SIZE_TILES][BunkerManager.BUNKER_SIZE_TILES];
        elements = new ArrayList<>();
        lock.unlock();
    }

    /**
     * Create a tile map from a serializer
     *
     * @param serializer Serializer
     * @param parent     Parent bunker
     */
    public TileMap(GravSerializer serializer, Bunker parent) {
        this(parent);
        lock.lock();
        deserializing.set(true);
        try {
            Tile[][] arr;
            Object[] o = serializer.readObject(parent);
            arr = new Tile[o.length][];
            for (int x = 0, oLength = o.length; x < oLength; x++) {
                Object[] o1 = (Object[]) o[x];
                arr[x] = new Tile[o1.length];
                for (int y = 0, o1Length = o1.length; y < o1Length; y++) {
                    Object o2 = o1[y];
                    arr[x][y] = (Tile) o2;
                }
            }

            for (int i = 0; i < arr.length; i++) {
                if (i >= tileMap.length) {
                    break;
                }
                Tile[] tileArr = arr[i];
                Tile[] nn = new Tile[BunkerManager.BUNKER_SIZE_TILES];
                System.arraycopy(tileArr, 0, nn, 0, Math.min(tileArr.length, nn.length));
                tileMap[i] = nn;
            }
            for (int x = 0; x < tileMap.length; x++) {
                Tile[] tileArr = tileMap[x];
                for (int y = 0; y < tileArr.length; y++) {
                    Tile tile = tileArr[y];
                    if (tile == null) {
                        continue;
                    }
                    BunkerElement el;
                    if ((el = tile.getParent()) == null) {
                        tileArr[y] = null;
                        continue;
                    }
                    if (!tile.getInternalPosition().equals(IntVector2D.ZERO))
                        continue;
                    for (int i2 = 0; i2 < el.getShape().getX(); i2++) {
                        for (int j2 = 0; j2 < el.getShape().getY(); j2++) {
                            try {
                                setOrMakeTile(i2 + x, j2 + y, el, new IntVector2D(i2, j2));
                            } catch (IllegalArgumentException e) {
                                System.out.println("Element Type: " + el.getType().toString() + " location: " + tile.getPosition().getX() + " (" + x + ") " + tile.getPosition().getY() + " (" + y + ") internal: " + i2 + " " + j2);
                                System.out.println("Could not set tile, out of bounds (" + (i2 + x) + ", " + (j2 + y) + ")");
                            }
                        }
                    }
                }
            }
        } finally {
            deserializing.set(false);
            lock.unlock();
        }
    }

    /**
     * Get a bunker element by its ID
     *
     * @param id Element ID
     * @return Element with that id or {@code null} if none exists
     */
    public BunkerElement byId(UUID id) {
        lock.lock();
        try {
            return elements.stream()
                    .filter(e -> e.getId().equals(id))
                    .findFirst()
                    .orElse(null);
        } finally {
            lock.unlock();
        }
    }

    public List<BunkerElement> getElements() {
        lock.lock();
        try {
            return new ArrayList<>(elements);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Get the element at a tile position
     *
     * @param vec Tile position
     * @return Element at that position or {@code null} if none exists
     */
    public BunkerElement byPosition(IntVector2D vec) {
        lock.lock();
        try {
            Tile tile = getTile(vec);
            if (tile == null) {
                return null;
            }
            return tile.getParent();
        } finally {
            lock.unlock();
        }
    }

    public <T extends BunkerElement> List<T> byClass(Class<T> clazz) {
        lock.lock();
        try {
            return elements.stream()
                    .filter(clazz::isInstance)
                    .map(clazz::cast)
                    .collect(Collectors.toList());
        } finally {
            lock.unlock();
        }
    }

    public Tile getTile(IntVector2D vec) {
        lock.lock();
        try {
            return getTile(vec.getX(), vec.getY());
        } finally {
            lock.unlock();
        }
    }

    public boolean isValidTilePosition(int x, int y) {
        return !(x < 0 || y < 0 || x >= tileMap.length || y >= tileMap[x].length);
    }

    public Tile getTile(int x, int y) {
        lock.lock();
        try {
            if (!isValidTilePosition(x, y))
                throw new IllegalArgumentException("Tile location out of bounds! (" + x + ", " + y + ")");
            return tileMap[x][y];
        } finally {
            lock.unlock();
        }
    }

    public Tile setOrMakeTile(IntVector2D vec, BunkerElement element, IntVector2D internalPosition) {
        return setOrMakeTile(vec.getX(), vec.getY(), element, internalPosition);
    }

    public Tile setOrMakeTile(int x, int y, BunkerElement element, IntVector2D internalPosition) {
        if (x < 0 || y < 0 || x >= tileMap.length || y >= tileMap[x].length)
            throw new IllegalArgumentException("Tile location out of bounds! (" + x + ", " + y + ")");
        if(internalPosition.equals(IntVector2D.ZERO)) {
            element.setPosition(new IntVector2D(x, y));
        }
        addElement(element);
        lock.lock();
        try {
            Tile tile = tileMap[x][y];
            if (tile == null) {
                tileMap[x][y] = new Tile(parent, element, internalPosition);
                return getTile(x, y);
            }
            tile.setParent(element);
            tile.setInternalPosition(internalPosition);
            return tile;
        } finally {
            lock.unlock();
        }
    }

    public IntVector3D getTileLocation(IntVector2D position) {
        return getTileLocation(position.getX(), position.getY());
    }

    public IntVector3D getTileLocation(int tx, int ty) {
        return this.parent.getBunkerLocation().add(tx * BunkerManager.TILE_SIZE_BLOCKS, 0, ty * BunkerManager.TILE_SIZE_BLOCKS);
    }

    public IntVector2D getTileLocation2D(IntVector2D position) {
        return getTileLocation2D(position.getX(), position.getY());
    }

    public IntVector2D getTileLocation2D(int tx, int ty) {
        return this.parent.getBunkerLocation().getXZ().add(new IntVector2D(tx, ty).multiply(BunkerManager.TILE_SIZE_BLOCKS));
    }

    public Tile[][] getTiles() {
        return tileMap;
    }

    @Override
    public void serialize(GravSerializer serializer) {
        Tile[][] map;
        lock.lock();
        map = new Tile[tileMap.length][];
        try {
            for (int i = 0; i < tileMap.length; i++) {
                map[i] = new Tile[tileMap[i].length];
                System.arraycopy(tileMap[i], 0, map[i], 0, tileMap[i].length);
            }
        } finally {
            lock.unlock();
        }
        serializer.writeObject(map);
    }

    /**
     * Tick all tickable elements that are currently loaded
     */
    public void tick() {
        lock.lock();
        List<BunkerElement> list = getElements();
        lock.unlock();
        for (BunkerElement element : list) {
            try {
                element.tick();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void removeTile(int x, int z) {
        lock.lock();
        try {
            tileMap[x][z] = null;
        } finally {
            lock.unlock();
        }
    }

    private void addElement(@NotNull BunkerElement el) {
        lock.lock();
        try {
            if (elements.contains(el)) {
                return;
            }
            elements.add(el);
            if(!deserializing.get()) {
                try {
                    el.onPlacement();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public void removeElement(int ex, int ez) {
        lock.lock();
        try {
            Tile tile = getTile(ex, ez);
            if (tile == null)
                return;
            BunkerElement element = tile.getParent();
            tile = getTile(element.getPosition());
            if (tile == null)
                throw new IllegalStateException("Complicated Situation: Tile has parent element1 but the tile at element1.getPosition() is null");

            IntVector2D basePos = tile.getInternalPosition().multiply(-1).add(new IntVector2D(ex, ez));
            for (int x = 0; x < element.getShape().getX(); x++) {
                for (int z = 0; z < element.getShape().getY(); z++) {
                    removeTile(basePos.getX() + x, basePos.getY() + z);
                }
            }
            element.remove();
            elements.remove(element);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Check if a BunkerElement can be placed at a certain position
     *
     * @param ex   Tile X Position
     * @param ez   Tile Z Position
     * @param size Size of the element
     * @return {@code true} if there are no other elements within the space
     */
    public boolean canPlace(int ex, int ez, IntVector2D size) {
        if (size == null)
            return false;
        lock.lock();
        try {
            for (int x = 0; x < size.getX(); x++) {
                for (int z = 0; z < size.getY(); z++) {

                    //Bounds check
                    if (!isValidTilePosition(ex + x, ez + z))
                        return false;

                    //Element check
                    Tile tile = getTile(ex + x, ez + z);
                    if (tile != null)
                        return false;
                }
            }
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Check if a BunkerElement can be placed at a certain position
     *
     * @param ex      Tile X Position
     * @param ez      Tile Z Position
     * @param element BunkerElement to check for
     * @return {@code true} if there are no other elements within the space
     */
    public boolean canPlace(int ex, int ez, BunkerElement element) {
        if (element == null)
            return false;
        return canPlace(ex, ez, element.getShape());
    }

    public boolean setElement(int ex, int ez, BunkerElement element) {
        if (element == null) {
            removeElement(ex, ez);
            return true;
        }
        if (!this.canPlace(ex, ez, element))
            return false;
        lock.lock();
        try {
            element.setPosition(new IntVector2D(ex, ez));
            for (int x = 0; x < element.getShape().getX(); x++) {
                for (int z = 0; z < element.getShape().getY(); z++) {
                    setOrMakeTile(ex + x, ez + z, element, new IntVector2D(x, z));
                }
            }
        } finally {
            lock.unlock();
        }
        return true;
    }

    public List<IntVector2D> getPlaceablePositions(IntVector2D size) {
        List<IntVector2D> possibles = new ArrayList<>();
        for (int x = 0; x < BunkerManager.BUNKER_SIZE_TILES - size.getX() + 1; x++) {
            for (int y = 0; y < BunkerManager.BUNKER_SIZE_TILES - size.getY() + 1; y++) {
                if (canPlace(x, y, size)) {
                    possibles.add(new IntVector2D(x, y));
                }
            }
        }
        return possibles;
    }

    public IntVector2D getRandomPlaceablePosition(IntVector2D size) {
        return ListUtil.randomElement(getPlaceablePositions(size));
    }

    public CuboidRegion getRegion() {
        Vector3D pos1 = new Vector3D(parent.getBunkerLocation().getX(), 0, parent.getBunkerLocation().getZ());
        Vector3D pos2 = pos1.add(tileMap.length * BunkerManager.TILE_SIZE_BLOCKS - 1, 255, tileMap[0].length * BunkerManager.TILE_SIZE_BLOCKS - 1);
        return new CuboidRegion(parent.getWorld().getBukkitWorld(), pos1, pos2);
    }

    public boolean isWithin(IntVector2D pos) {
        if (pos.getX() < 0)
            return false;
        if (pos.getY() < 0)
            return false;
        if (pos.getX() >= BunkerManager.BUNKER_SIZE_TILES)
            return false;
        if (pos.getY() >= BunkerManager.BUNKER_SIZE_TILES)
            return false;
        return true;
    }
}
