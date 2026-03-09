package util;

public class EasingUtils {

    // Constants for Circular function calculations
    private static final float C1 = 1.70158f;
    private static final float C2 = C1 * 1.525f;
    private static final float C3 = C1 + 1;
    private static final float C4 = (float) ((2f * Math.PI) / 3f);
    private static final float C5 = (float) ((2f * Math.PI) / 4.5f);
    private static final float N1 = 7.5625f;
    private static final float D1 = 2.75f;

    public static float moveTowards(float current, float target){
        return moveTowards(current, target, 0.001f, 0.0001f);
    }

    public static float moveTowards(float current, float target, float increment, float epsilon){

        if(current == target){
            return current;
        }

        float delta = (target - current) * increment;

        // If we are within delta + epsilon
        if(current < target && current + Math.abs(delta) + epsilon >= target){
            current = target;
            return current;
        }

        // moveCurrent towards target
        current += delta;

        if(current > target){
            current = target;
        }

        return current;
    }

    public static float easeBetween(float value1, float value2, float delta, EnumInterpolation easingFunction){

        // Clamp our input range to between Zero and 1
        float mu = Math.min(Math.max(delta, 0f), 1f);

        switch (easingFunction){
            case LINEAR:{
                //Linear interpolation
                float mu_prime = (1.0f - mu);
                return (mu_prime * value1) + (mu * value2);
            }

            case COSINE:{
                float mu_prime = (float) ((1f-Math.cos(mu*Math.PI))/2f);
                return (value1*(1f-mu_prime)+value2*mu_prime);
            }

            case EASE_IN_SIN: {
                float mu_sin = (float) (1f - Math.cos((mu * Math.PI) / 2f));
                float mu_sin_prime = (1f - mu_sin);
                return (mu_sin_prime * value1) + (mu_sin * value2);
            }
            case EASE_OUT_SIN: {
                float mu_sin = (float) Math.sin((mu * Math.PI) / 2f);
                float mu_sin_prime = (1f - mu_sin);
                return (mu_sin_prime * value1) + (mu_sin * value2);
            }
            case EASE_IN_OUT_SIN: {
                float mu_sin = (float) (-(Math.cos(Math.PI * mu) - 1f) / 2f);
                float mu_sin_prime = (1f - mu_sin);
                return (mu_sin_prime * value1) + (mu_sin * value2);
            }

            case EASE_IN_QUAD: {
                float mu_quad = mu * mu;
                float mu_quad_prime = (1f - mu_quad);
                return (mu_quad_prime * value1) + (mu_quad * value2);
            }
            case EASE_OUT_QUAD: {
                float mu_quad = 1f - (1f - mu) * (1f - mu);
                float mu_quad_prime = (1f - mu_quad);
                return (mu_quad_prime * value1) + (mu_quad * value2);
            }
            case EASE_IN_OUT_QUAD: {
                float mu_quad = mu < 0.5f ? 2f * mu * mu : (float) (1f - Math.pow(-2f * mu + 2f, 2f) / 2f);
                float mu_quad_prime = (1f - mu_quad);
                return (mu_quad_prime * value1) + (mu_quad * value2);
            }

            case EASE_IN_CUBIC:{
                float mu_cubic = mu * mu * mu;
                float mu_cubic_prime = (1f - mu_cubic);
                return (mu_cubic_prime * value1) + (mu_cubic * value2);
            }
            case EASE_OUT_CUBIC: {
                float mu_cubic = (float) (1f - Math.pow(1f - mu, 3));
                float mu_cubic_prime = (1f - mu_cubic);
                return (mu_cubic_prime * value1) + (mu_cubic * value2);
            }
            case EASE_IN_OUT_CUBIC: {
                float mu_cubic = mu < 0.5f ? 4f * mu * mu * mu : (float) (1f - Math.pow(-2f * mu + 2f, 3) / 2f);
                float mu_cubic_prime = (1f - mu_cubic);
                return (mu_cubic_prime * value1) + (mu_cubic * value2);
            }

            case EASE_IN_QUARTIC: {
                float mu_quartic = mu * mu * mu * mu;
                float mu_quartic_prime = (1f - mu_quartic);
                return (mu_quartic_prime * value1) + (mu_quartic * value2);
            }
            case EASE_OUT_QUARTIC:{
                float mu_quartic = (float) (1f - Math.pow(1f - mu, 4));
                float mu_quartic_prime = (1f - mu_quartic);
                return (mu_quartic_prime * value1) + (mu_quartic * value2);
            }
            case EASE_IN_OUT_QUARTIC:{
                float mu_quartic = mu < 0.5f ? 8f * mu * mu * mu * mu : (float) (1f - Math.pow(-2f * mu + 2f, 4) / 2f);
                float mu_quartic_prime = (1f - mu_quartic);
                return (mu_quartic_prime * value1) + (mu_quartic * value2);
            }

            case EASE_IN_QUINTIC:{
                float mu_quintic = mu * mu * mu * mu * mu;
                float mu_quintic_prime = (1f - mu_quintic);
                return (mu_quintic_prime * value1) + (mu_quintic * value2);
            }
            case EASE_OUT_QUINTIC:{
                float mu_quintic = (float) (1f - Math.pow(1f - mu, 5));
                float mu_quintic_prime = (1f - mu_quintic);
                return (mu_quintic_prime * value1) + (mu_quintic * value2);
            }
            case EASE_IN_OUT_QUINTIC:{
                float mu_quintic = mu < 0.5f ? 16f * mu * mu * mu * mu * mu : (float) (1f - Math.pow(-2f * mu + 2f, 5) / 2f);
                float mu_quintic_prime = (1f - mu_quintic);
                return (mu_quintic_prime * value1) + (mu_quintic * value2);
            }

            case EASE_IN_EXPONENTIAL:{
                float mu_exponential = mu == 0 ? 0f : (float) Math.pow(2f, 10f * mu - 10f);
                float mu_exponential_prime = (1f - mu_exponential);
                return (mu_exponential_prime * value1) + (mu_exponential * value2);
            }
            case EASE_OUT_EXPONENTIAL:{
                float mu_exponential = mu == 1 ? 1f : (float) (1f - Math.pow(2f, -10f * mu));
                float mu_exponential_prime = (1f - mu_exponential);
                return (mu_exponential_prime * value1) + (mu_exponential * value2);
            }
            case EASE_IN_OUT_EXPONENTIAL:{
                float mu_exponential = mu == 0 ? 0f : (float) (mu == 1 ? 1f : mu < 0.5f ? Math.pow(2f, 20f * mu - 10f) / 2f : (2f - Math.pow(2f, -20f * mu + 10f)) / 2f);
                float mu_exponential_prime = (1f - mu_exponential);
                return (mu_exponential_prime * value1) + (mu_exponential * value2);
            }

            case EASE_IN_CIRCULAR:{
                float mu_circular = (float) (1f - Math.sqrt(1f - Math.pow(mu, 2)));
                float mu_circular_prime = (1f - mu_circular);
                return (mu_circular_prime * value1) + (mu_circular * value2);
            }
            case EASE_OUT_CIRCULAR:{
                float mu_circular = (float) Math.sqrt(1f - Math.pow(mu - 1f, 2));
                float mu_circular_prime = (1f - mu_circular);
                return (mu_circular_prime * value1) + (mu_circular * value2);
            }
            case EASE_IN_OUT_CIRCULAR:{
                float mu_circular = (float) (mu < 0.5 ? (1f - Math.sqrt(1f - Math.pow(2f * mu, 2))) / 2f : (Math.sqrt(1f - Math.pow(-2f * mu + 2f, 2)) + 1f) / 2f);
                float mu_circular_prime = (1f - mu_circular);
                return (mu_circular_prime * value1) + (mu_circular * value2);
            }

            case EASE_IN_BACK:{
                float mu_back = C3 * mu * mu * mu - C1 * mu * mu;
                float mu_back_prime = (1f - mu_back);
                return (mu_back_prime * value1) + (mu_back * value2);
            }
            case EASE_OUT_BACK:{
                float mu_back = (float) (1f + C3 * Math.pow(mu - 1f, 3) + C1 * Math.pow(mu - 1f, 2));
                float mu_back_prime = (1f - mu_back);
                return (mu_back_prime * value1) + (mu_back * value2);
            }
            case EASE_IN_OUT_BACK:{
                float mu_back = (float) (mu < 0.5
                    ? (Math.pow(2f * mu, 2) * ((C2 + 1f) * 2f * mu - C2)) / 2f
                    : (Math.pow(2f * mu - 2f, 2) * ((C2 + 1f) * (mu * 2f - 2f) + C2) + 2f) / 2f);
                float mu_back_prime = (1f - mu_back);
                return (mu_back_prime * value1) + (mu_back * value2);
            }

            case EASE_IN_ELASTIC:{
                float mu_elastic = mu == 0
                        ? 0f
                        : (float) (mu == 1
                        ? 1f
                        : -Math.pow(2f, 10f * mu - 10f) * Math.sin((mu * 10f - 10.75f) * C4));
                float mu_elastic_prime = (1f - mu_elastic);
                return (mu_elastic_prime * value1) + (mu_elastic * value2);
            }
            case EASE_OUT_ELASTIC:{
                float mu_elastic = mu == 0
                        ? 0f
                        : (float) (mu == 1
                        ? 1f
                        : Math.pow(2f, -10f * mu) * Math.sin((mu * 10f - 0.75f) * C4) + 1f);
                float mu_elastic_prime = (1f - mu_elastic);
                return (mu_elastic_prime * value1) + (mu_elastic * value2);
            }
            case EASE_IN_OUT_ELASTIC:{
                float mu_elastic = mu == 0
                        ? 0f
                        : (float) (mu == 1
                        ? 1f
                        : mu < 0.5f
                        ? -(Math.pow(2f, 20f * mu - 10f) * Math.sin((20f * mu - 11.125f) * C5)) / 2f
                        : (Math.pow(2f, -20f * mu + 10f) * Math.sin((20f * mu - 11.125f) * C5)) / 2f + 1f);
                float mu_elastic_prime = (1f - mu_elastic);
                return (mu_elastic_prime * value1) + (mu_elastic * value2);
            }

            case EASE_IN_BOUNCE:{

                float mu_bounce_out;
                float mu_bounce_out_input = (1f - mu);

                loop:{
                    if (mu_bounce_out_input < (1f / D1)) {
                        mu_bounce_out = N1 * mu_bounce_out_input * mu_bounce_out_input;
                        break loop;
                    } else if (mu_bounce_out_input < 2f / D1) {
                        mu_bounce_out = N1 * (mu_bounce_out_input -= 1.5f / D1) * mu_bounce_out_input + 0.75f;
                        break loop;
                    } else if (mu_bounce_out_input < 2.5f / D1) {
                        mu_bounce_out = N1 * (mu_bounce_out_input -= 2.25f / D1) * mu_bounce_out_input + 0.9375f;
                        break loop;
                    } else {
                        mu_bounce_out = N1 * (mu_bounce_out_input -= 2.625f / D1) * mu_bounce_out_input + 0.984375f;
                        break loop;
                    }
                }

                float mu_bounce = 1f - mu_bounce_out;
                float mu_bounce_prime = (1f - mu_bounce);
                return (mu_bounce_prime * value1) + (mu_bounce * value2);
            }

            case EASE_OUT_BOUNCE:{
                float mu_bounce;

                loop:{
                    if (mu < (1f / D1)) {
                        mu_bounce = N1 * mu * mu;
                        break loop;
                    } else if (mu < 2f / D1) {
                        mu_bounce = N1 * (mu -= 1.5f / D1) * mu + 0.75f;
                        break loop;
                    } else if (mu < 2.5f / D1) {
                        mu_bounce = N1 * (mu -= 2.25f / D1) * mu + 0.9375f;
                        break loop;
                    } else {
                        mu_bounce = N1 * (mu -= 2.625f / D1) * mu + 0.984375f;
                        break loop;
                    }
                }

                float mu_bounce_prime = (1f - mu_bounce);
                return (mu_bounce_prime * value1) + (mu_bounce * value2);
            }

            case EASE_IN_OUT_BOUNCE:{
                float mu_bounce_out;
                float mu_bounce_out_input;

                if(mu < 0.5f){
                    mu_bounce_out_input = 1f - 2f * mu;
                }else{
                    mu_bounce_out_input = 2f * mu - 1f;
                }

                loop:{
                    if (mu_bounce_out_input < (1f / D1)) {
                        mu_bounce_out = N1 * mu_bounce_out_input * mu_bounce_out_input;
                        break loop;
                    } else if (mu_bounce_out_input < 2f / D1) {
                        mu_bounce_out = N1 * (mu_bounce_out_input -= 1.5f / D1) * mu_bounce_out_input + 0.75f;
                        break loop;
                    } else if (mu_bounce_out_input < 2.5f / D1) {
                        mu_bounce_out = N1 * (mu_bounce_out_input -= 2.25f / D1) * mu_bounce_out_input + 0.9375f;
                        break loop;
                    } else {
                        mu_bounce_out = N1 * (mu_bounce_out_input -= 2.625f / D1) * mu_bounce_out_input + 0.984375f;
                        break loop;
                    }
                }

                float mu_bounce = mu < 0.5f ? (1f - mu_bounce_out) / 2f : (1f + mu_bounce_out) / 2f;

                float mu_bounce_prime = (1f - mu_bounce);
                return (mu_bounce_prime * value1) + (mu_bounce * value2);
            }
        }

        return 0;
    }

    public static float[] generateNPointsOnSphere(int samples){
        // XYZ
        float[] points = new float[samples * 3];

        // Golden Ratio
        float phi = (float) (Math.PI * (3f - Math.sqrt(5f)));

        for(int i = 0; i < samples; i++){

            float y = 1f - (((float)i) / (((float) samples) - 1f)) * 2f;
            float radius = (float) Math.sqrt(1f - (y * y));
            float theta = phi * (float) i;

            float x = (float) (Math.cos(theta) * radius);
            float z = (float) (Math.sin(theta) * radius);

            points[(i * 3) + 0] = x;
            points[(i * 3) + 1] = y;
            points[(i * 3) + 2] = z;
        }

        return points;
    }

}