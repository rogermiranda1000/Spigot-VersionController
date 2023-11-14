package com.rogermiranda1000.versioncontroller.entities.bounds;

import com.rogermiranda1000.versioncontroller.VersionController;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class EntityBoundsGetterPre14 implements EntityBoundsGetter {
    @Nullable
    private static final Class<?> craftEntityClass = EntityBoundsGetterPre14.getCraftEntityClass(),
                                entityClass = EntityBoundsGetterPre14.getEntityClass(),
                                boundingBoxClass = EntityBoundsGetterPre14.getBoundingBoxClass();

    @Nullable
    private static final Method getEntityMethod = EntityBoundsGetterPre14.getEntityMethod(),
            getBoundingBoxMethod = EntityBoundsGetterPre14.getBoundingBoxMethod();

    @Nullable
    private static final Field minXField = getBoundingBoxField("minX"), minYField = getBoundingBoxField("minY"), minZField = getBoundingBoxField("minZ"),
            maxXField = getBoundingBoxField("maxX"), maxYField = getBoundingBoxField("maxY"), maxZField = getBoundingBoxField("maxZ");

    private static Class<?> getCraftEntityClass() {
        try {
            return Class.forName(VersionController.bukkitPackage + ".entity.CraftEntity");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Class<?> getEntityClass() {
        try {
            return Class.forName(VersionController.nmsPackage + ".Entity");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Class<?> getBoundingBoxClass() {
        try {
            return Class.forName(VersionController.nmsPackage + ".AxisAlignedBB");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Method getBoundingBoxMethod() {
        try {
            return entityClass.getMethod("getBoundingBox");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Method getEntityMethod() {
        try {
            return craftEntityClass.getMethod("getHandle");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @param name min/max x/y/z
     */
    private static Field getBoundingBoxField(String name) {
        try {
            return EntityBoundsGetterPre14.boundingBoxClass.getDeclaredField(name);
        } catch (NoSuchFieldException ignored) {
            try {
                // maybe it's a/b/c/d/e/f
                int var = (int)'a';
                if (name.startsWith("max")) var += 3;
                var += (int)name.charAt(3) - 'X';
                return EntityBoundsGetterPre14.boundingBoxClass.getDeclaredField(Character.toString((char)var));
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    @Override
    public BoundingBox getBoundingBox(Entity e) {
        try {
            Object craftEntity = EntityBoundsGetterPre14.craftEntityClass.cast(e);
            Object boundingBox = EntityBoundsGetterPre14.getBoundingBoxMethod.invoke(EntityBoundsGetterPre14.getEntityMethod.invoke(craftEntity));
            return new BoundingBox((double)minXField.get(boundingBox), (double)minYField.get(boundingBox), (double)minZField.get(boundingBox),
                    (double)maxXField.get(boundingBox), (double)maxYField.get(boundingBox), (double)maxZField.get(boundingBox));
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
