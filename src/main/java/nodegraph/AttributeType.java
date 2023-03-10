package nodegraph;

import graphics.GLStruct;
import graphics.SSBO;

public enum AttributeType {
    // Numerics
    INT((to_compare) -> {
        return false;
    }),
    FLOAT((to_compare) -> {
        return false;
    }),
    NUMERIC((to_compare) -> {
        return false;
    }),

    VEC2 ((to_compare) -> {
        return false;
    }),
    VEC3 ((to_compare) -> {
        return false;
    }),
    VEC4 ((to_compare) -> {
        return false;
    }),
    VECTOR((to_compare) -> {
        return false;
    }),

    //Matrix
    MAT2((to_compare) -> {
        return false;
    }),
    MAT3((to_compare) -> {
        return false;
    }),
    MAT4((to_compare) -> {
        return false;
    }),
    MATRIX((to_compare) -> {
        return false;
    }),

    // Samplers hold images.
    SAMPLER_CUBE((to_compare) -> {
        return false;
    }),
    SAMPLER_2D((to_compare) -> {
        return false;
    }),
    SAMPLER_3D((to_compare) -> {
        return false;
    }),
    SAMPLER((to_compare) -> {
        return false;
    }),

    //Bool
    BOOL((to_compare) -> {
        return false;
    }),

    STRING((to_compare) -> {
        return false;
    }),

    AGENT((to_compare) -> {
        return to_compare instanceof SSBO;
    }),

    ANY((to_compare) -> {
        return true;
    }),

    UNKNOWN((to_compare) -> {
        return true;
    }),
    ;
    // Define a comparison function
    @FunctionalInterface
    public interface TypeComparison {
        boolean structIsInstanceOf(GLStruct to_compare);
    }

    protected TypeComparison comparison;

    AttributeType(TypeComparison comparison) {
        this.comparison = comparison;
    }
}
