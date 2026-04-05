package com.mountrich.krushimitra.crop_diseast_detection_api;

import java.util.HashMap;
import java.util.Map;

public class DiseaseDatabase {

    public static Map<String, String> getDiseaseData() {

        Map<String, String> map = new HashMap<>();

        // 🌿 POTATO
        map.put("solanum tuberosum",
                "Crop: Potato\nDisease: Early Blight\nSolution: Use fungicide, avoid overwatering");

        // 🌿 TOMATO
        map.put("solanum lycopersicum",
                "Crop: Tomato\nDisease: Leaf Curl Virus\nSolution: Remove infected leaves, control insects");

        // 🌿 RICE
        map.put("oryza sativa",
                "Crop: Rice\nDisease: Bacterial Leaf Blight\nSolution: Use resistant seeds, proper drainage");

        // 🌿 WHEAT
        map.put("triticum aestivum",
                "Crop: Wheat\nDisease: Rust Disease\nSolution: Spray fungicide, use resistant variety");

        // 🌿 MAIZE
        map.put("zea mays",
                "Crop: Maize\nDisease: Leaf Spot\nSolution: Crop rotation, fungicide spray");

        // 🌿 COTTON
        map.put("gossypium",
                "Crop: Cotton\nDisease: Wilt\nSolution: Soil treatment, resistant seeds");

        // 🌿 SUGARCANE
        map.put("saccharum officinarum",
                "Crop: Sugarcane\nDisease: Red Rot\nSolution: Remove infected plants, crop rotation");

        // 🌿 CHILLI
        map.put("capsicum annuum",
                "Crop: Chilli\nDisease: Leaf Curl\nSolution: Use insecticide, remove infected plants");

        // 🌿 BRINJAL
        map.put("solanum melongena",
                "Crop: Brinjal\nDisease: Fruit Borer\nSolution: Use neem oil, pesticides");

        // 🌿 ONION
        map.put("allium cepa",
                "Crop: Onion\nDisease: Purple Blotch\nSolution: Fungicide spray");

        // 🌿 SOYBEAN
        map.put("glycine max",
                "Crop: Soybean\nDisease: Yellow Mosaic\nSolution: Control whiteflies");

        // 🌿 GROUNDNUT
        map.put("arachis hypogaea",
                "Crop: Groundnut\nDisease: Leaf Spot\nSolution: Use fungicide");

        // 🌿 BANANA
        map.put("musa",
                "Crop: Banana\nDisease: Panama Disease\nSolution: Use resistant variety");

        // 🌿 MANGO
        map.put("mangifera indica",
                "Crop: Mango\nDisease: Powdery Mildew\nSolution: Fungicide spray");

        // 🌿 GRAPES
        map.put("vitis vinifera",
                "Crop: Grapes\nDisease: Downy Mildew\nSolution: Proper air circulation, fungicide");

        return map;
    }
}