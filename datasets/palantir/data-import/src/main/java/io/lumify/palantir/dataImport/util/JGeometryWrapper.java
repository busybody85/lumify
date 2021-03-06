package io.lumify.palantir.dataImport.util;

import io.lumify.core.exception.LumifyException;

import java.awt.geom.Point2D;
import java.lang.reflect.Method;
import java.sql.Struct;

/**
 * This class exists so that to compile and run Lumify you don't have to setup the oracle Jars.
 */
public class JGeometryWrapper {
    public static final String ORACLE_SQL_STRUCT = "oracle.sql.STRUCT";
    public static final String ORACLE_SPATIAL_GEOMETRY_JGEOMETRY = "oracle.spatial.geometry.JGeometry";
    public static final int GTYPE_POINT = 1;
    public static final int GTYPE_CURVE = 2;
    public static final int GTYPE_POLYGON = 3;
    public static final int GTYPE_COLLECTION = 4;
    public static final int GTYPE_MULTIPOINT = 5;
    public static final int GTYPE_MULTICURVE = 6;
    public static final int GTYPE_MULTIPOLYGON = 7;

    public enum Type {
        POINT(1),
        CURVE(2),
        POLYGON(3),
        COLLECTION(4),
        MULTIPOINT(5),
        MULTICURVE(6),
        MULTIPOLYGON(7);

        private final int value;

        Type(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    private static final Class jGeometryClass;
    private static final Class oracleSqlStructClass;
    private final Object jGeometryObj;

    static {
        try {
            oracleSqlStructClass = Class.forName(ORACLE_SQL_STRUCT);
            jGeometryClass = Class.forName(ORACLE_SPATIAL_GEOMETRY_JGEOMETRY);
        } catch (ClassNotFoundException e) {
            throw new LumifyException("Could not load " + ORACLE_SPATIAL_GEOMETRY_JGEOMETRY + " class", e);
        }
    }

    public JGeometryWrapper(Object jGeometryObj) {
        this.jGeometryObj = jGeometryObj;
    }

    public static JGeometryWrapper load(Struct st) {
        try {
            if (st == null) {
                return null;
            }
            Method m = jGeometryClass.getDeclaredMethod("load", oracleSqlStructClass);
            Object obj = m.invoke(null, st);
            return new JGeometryWrapper(obj);
        } catch (Exception ex) {
            throw new LumifyException("Could not load JGeometry", ex);
        }
    }

    public Type getType() {
        int type;
        try {
            Method m = jGeometryClass.getMethod("getType");
            type = (Integer) m.invoke(this.jGeometryObj);
            for (Type t : Type.values()) {
                if (t.getValue() == type) {
                    return t;
                }
            }
        } catch (Exception ex) {
            throw new LumifyException("Could not getType", ex);
        }
        throw new LumifyException("Unknown type: " + type);
    }

    public Point2D getJavaPoint() {
        try {
            Method m = jGeometryClass.getMethod("getJavaPoint");
            return (Point2D) m.invoke(this.jGeometryObj);
        } catch (Exception ex) {
            throw new LumifyException("Could not getJavaPoint", ex);
        }
    }
}
