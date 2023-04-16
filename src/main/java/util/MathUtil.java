package util;

import org.joml.Vector2i;

import java.util.*;

public class MathUtil {
    public static int[] factor (int input){
        // We only need to check integers up to the sqrt of the number.
        int upper_bounds = (int) Math.ceil(Math.sqrt(input));
        // Define our output set
        LinkedList<Integer> factors = new LinkedList<>();
        for(int i = 1; i <= upper_bounds; i++){
            if(input % i == 0){
                // We have a factor of the number.
                factors.add(i);
                factors.add(input / i);
            }
        }
        // Copy to out array
        int[] output = new int[factors.size()];
        for(int i = 0; i < factors.size(); i++){
            output[i] = factors.get(i);
        }
        Arrays.sort(output);
        // Sort out array
        return output;
    }

    /**
     * The goal of this function is to find a rectangle such that the area of the rectangle equals the area_of_rect variable, however the width of that rectangle is divisible by width and the height of that rectangle is divisible by height.
     * @param width
     * @param height
     * @param area_of_rect
     * @return
     */
    public static Vector2i find_optimal_tiling(int width, int height, int area_of_rect){
        int[] width_factors = factor(width);
        int[] height_factors = factor(height);
        int[] area_factors = factor(area_of_rect);
        // Now we look through all factors of the area_of_rect
        for(int i = 0; i < area_factors.length / 2; i++){
            int lesser_factor = area_factors[area_factors.length / 2 - 1 - i];
            int greater_factor = area_factors[area_factors.length / 2 + i];
            if(indexOf(width_factors, lesser_factor) >=0 && indexOf(height_factors, greater_factor) >= 0){
                // If this condition is met we have found an optimal rectangle to fill our grid with.
                return new Vector2i(lesser_factor, greater_factor);
            }
        }
        // We cannot perfectly optimize the grid in this case, we need to find the best of the worse options.
        // If we get to this point, we have a chance of optimization still. We need to compute the GCM of width and height whos value^2 < area_of_rect

        for(int factor_index = 0; factor_index > width_factors.length; factor_index++){
            int factor = width_factors[width_factors.length - 1 - factor_index];
            if(indexOf(height_factors, factor) > 0){ // This is a common multiple of both dimensions
                if(factor * factor <= area_of_rect){
                    // This is a better option than 1,1
                    return new Vector2i(factor, factor);
                }
            }
        }

        if(area_of_rect > 0 ){
            return find_optimal_tiling(width, height, area_of_rect - 1);
        }else{
            // This is the least optimal sub rectangle we could make grid with, but it ALWAYS works to fill the screen without overlap or overflow or underflow because every natural number is divisible by 1.
            return new Vector2i(1, 1);
        }
    }

    /**
     * This function searches through an array of numbers and finds the index of the element_to_find element in the array_to_search array.
     * @param array_to_search This needs to be a sorted array.
     * @param element_to_find This is the element we are looking for.
     * @return Returns the index of the elemement in the array or -1 if no such index is found.
     */
    public static int indexOf(int[] array_to_search, int element_to_find){
        // Look through every element
        int i = 0;
        for(int element_to_compare : array_to_search){
            // We can break this loop early if the element we are comparing is larger than the element we are finding.
            // NOTE we can only make this optimization because we are passed a sorted array.
            if(element_to_compare > element_to_find){
                break;
            }
            // check if we have found the number we are looking for.
            if(element_to_compare == element_to_find){
                return i;
            }
            // We didnt find the element we were looking for, lets increment i and try again.
            i++;
        }
        return -1;
    }

    public static int computeAlignment(int bytes, int value) {
        return value - (bytes % value);
    }

    public static boolean isMultipleOf(int bytes, int i) {
        return bytes % i == 0;
    }

    public static boolean isFactorOf(int bytes, int i) {
        if (bytes <= 0 || bytes > i) {
            return false;
        }

        int[] factors = factor(i);
        for(int factor : factors){

            if(factor == bytes){
                return true;
            }

            if(factor > bytes){
                return false;
            }
        }
        return false;
    }
}
