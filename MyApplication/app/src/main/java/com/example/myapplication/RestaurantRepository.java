package com.example.myapplication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class RestaurantRepository {

    private static final List<RestaurantInfo> RESTAURANTS = buildRestaurants();

    private RestaurantRepository() {}

    public static List<RestaurantInfo> getRestaurants() {
        return new ArrayList<>(RESTAURANTS);
    }

    public static RestaurantInfo getRestaurantById(String id) {
        if (id == null) return null;
        for (RestaurantInfo r : RESTAURANTS) {
            if (r.getId().equals(id)) return r;
        }
        return null;
    }

    private static List<RestaurantInfo> buildRestaurants() {
        List<RestaurantInfo> restaurants = new ArrayList<>();

        restaurants.add(new RestaurantInfo(
                "agidel",
                "Ресторан \"Агидель\"",
                "Уфа, ул. Ленина, 16",
                "Домашняя башкирская кухня и уютные семейные ужины.",
                R.drawable.agidel_menu,   // <-- menu image
                new RoomInfo("Вход", 0.2f, 1.00f, 130, 50),
                new RoomInfo("Туалет", 0.8f, 1.00f, 130, 50),
                new RoomInfo("Кухня", 1.00f, 0.00f, 190, 230),
                Arrays.asList(
                        new TableInfo("Стол 1", 5, 0.05f, 0.02f),
                        new TableInfo("Стол 2", 5, 0.05f, 0.17f),
                        new TableInfo("Стол 3", 5, 0.05f, 0.32f),
                        new TableInfo("Стол 4", 4, 0.05f, 0.53f),
                        new TableInfo("Стол 5", 4, 0.50f, 0.53f),
                        new TableInfo("Стол 6", 4, 0.95f, 0.53f),
                        new TableInfo("Стол 7", 4, 0.05f, 0.69f),
                        new TableInfo("Стол 8", 4, 0.50f, 0.69f),
                        new TableInfo("Стол 9", 4, 0.95f, 0.69f),
                        new TableInfo("Стол 10", 4, 0.05f, 0.85f),
                        new TableInfo("Стол 11", 4, 0.50f, 0.85f),
                        new TableInfo("Стол 12", 4, 0.95f, 0.85f)
                )
        ));

        restaurants.add(new RestaurantInfo(
                "bashkort",
                "Ресторан \"Башкорт Йорто\"",
                "Уфа, пр. Октября, 88",
                "Национальные блюда и живая атмосфера.",
                R.drawable.bashkort_menu, // <-- menu image
                new RoomInfo("Вход", 1.05f, 0.95f, 50, 110),
                new RoomInfo("Туалет", 1.05f, 0.7f, 120, 60),
                new RoomInfo("Кухня", 1.05f, 0.05f, 120, 360),
                Arrays.asList(
                        new TableInfo("Стол 1", 4, 0.05f, 0.05f),
                        new TableInfo("Стол 2", 2, 0.48f, 0.05f),
                        new TableInfo("Стол 3", 4, 0.05f, 0.20f),
                        new TableInfo("Стол 4", 2, 0.48f, 0.20f),
                        new TableInfo("Стол 5", 4, 0.05f, 0.35f),
                        new TableInfo("Стол 6", 2, 0.48f, 0.35f),
                        new TableInfo("Стол 7", 4, 0.05f, 0.50f),
                        new TableInfo("Стол 8", 2, 0.48f, 0.50f),
                        new TableInfo("Стол 9", 6, 0.05f, 0.70f),
                        new TableInfo("Стол 10", 6, 0.05f, 0.88f)
                )
        ));

        restaurants.add(new RestaurantInfo(
                "ural",
                "Ресторан \"Уральский Вечер\"",
                "Уфа, ул. Комсомольская, 41",
                "Европейское меню и камерная обстановка.",
                R.drawable.ural_menu,     // <-- menu image
                new RoomInfo("Вход", 0.5f, 1.00f, 140, 50),
                new RoomInfo("Туалет", 1.00f, -0.02f, 125, 80),
                new RoomInfo("Кухня", -0.05f, -0.02f, 225, 80),
                Arrays.asList(
                        new TableInfo("Стол 1", 4, 0.05f, 0.22f),
                        new TableInfo("Стол 2", 4, 0.50f, 0.22f),
                        new TableInfo("Стол 3", 4, 0.95f, 0.22f),
                        new TableInfo("Стол 4", 4, 0.05f, 0.42f),
                        new TableInfo("Стол 5", 4, 0.50f, 0.42f),
                        new TableInfo("Стол 6", 4, 0.95f, 0.42f),
                        new TableInfo("Стол 7", 4, 0.05f, 0.62f),
                        new TableInfo("Стол 8", 4, 0.50f, 0.62f),
                        new TableInfo("Стол 9", 4, 0.95f, 0.62f),
                        new TableInfo("Стол 10", 4, 0.05f, 0.82f),
                        new TableInfo("Стол 11", 4, 0.50f, 0.82f),
                        new TableInfo("Стол 12", 4, 0.95f, 0.82f)
                )
        ));

        return restaurants;
    }

    // ---------- Inner data classes ----------

    public static class RestaurantInfo {
        private final String id, name, address, description;
        private final int menuImageResId;                    // <-- NEW field
        private final RoomInfo entrance, restroom, kitchen;
        private final List<TableInfo> tables;

        // Updated constructor with menuImageResId
        RestaurantInfo(String id, String name, String address, String description,
                       int menuImageResId,
                       RoomInfo entrance, RoomInfo restroom, RoomInfo kitchen,
                       List<TableInfo> tables) {
            this.id = id;
            this.name = name;
            this.address = address;
            this.description = description;
            this.menuImageResId = menuImageResId;           // <-- store it
            this.entrance = entrance;
            this.restroom = restroom;
            this.kitchen = kitchen;
            this.tables = tables;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getAddress() { return address; }
        public String getDescription() { return description; }
        public int getMenuImageResId() { return menuImageResId; }  // <-- getter
        public RoomInfo getEntrance() { return entrance; }
        public RoomInfo getRestroom() { return restroom; }
        public RoomInfo getKitchen() { return kitchen; }
        public List<TableInfo> getTables() { return tables; }
    }

    public static class TableInfo {
        private final String name;
        private final int capacity;
        private final float xPercent, yPercent;

        TableInfo(String n, int c, float x, float y) {
            this.name = n;
            this.capacity = c;
            this.xPercent = x;
            this.yPercent = y;
        }

        public String getName() { return name; }
        public int getCapacity() { return capacity; }
        public float getXPercent() { return xPercent; }
        public float getYPercent() { return yPercent; }
    }

    public static class RoomInfo {
        private final String label;
        private final float xPercent, yPercent;
        private final int widthDp, heightDp;

        RoomInfo(String l, float x, float y, int w, int h) {
            this.label = l;
            this.xPercent = x;
            this.yPercent = y;
            this.widthDp = w;
            this.heightDp = h;
        }

        public String getLabel() { return label; }
        public float getXPercent() { return xPercent; }
        public float getYPercent() { return yPercent; }
        public int getWidthDp() { return widthDp; }
        public int getHeightDp() { return heightDp; }
    }
}