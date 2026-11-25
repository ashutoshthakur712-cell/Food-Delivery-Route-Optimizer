import java.util.*;

public class Main {

    static final String RESTAURANT = "R";

    // ---------- Graph edge (road) ----------
    static class Edge {
        String to;
        int km;

        Edge(String to, int km) {
            this.to = to;
            this.km = km;
        }
    }

    // ---------- Heap node used by Dijkstra ----------
    static class Node implements Comparable<Node> {
        String name;
        int dist;

        Node(String name, int dist) {
            this.name = name;
            this.dist = dist;
        }

        @Override
        public int compareTo(Node o) {
            if (dist != o.dist) return Integer.compare(dist, o.dist);
            return name.compareTo(o.name);   // tie -> alphabetical
        }
    }

    // Weighted graph (adjacency list)
    static HashMap<String, List<Edge>> graph = new HashMap<>();
    // location -> list of order IDs to be delivered there
    static HashMap<String, List<String>> ordersByLocation = new HashMap<>();
    // order ID -> location (used to avoid duplicate orders)
    static HashMap<String, String> orderLocation = new HashMap<>();
    static Scanner sc = new Scanner(System.in);

    // ---------- Road map ----------
    static void addRoad(String a, String b, int km) {
        graph.computeIfAbsent(a, k -> new ArrayList<>()).add(new Edge(b, km));
        graph.computeIfAbsent(b, k -> new ArrayList<>()).add(new Edge(a, km));
    }

    static void buildMap() {
        addRoad("R", "A", 2);
        addRoad("A", "B", 3);
        addRoad("B", "C", 2);
        addRoad("R", "C", 4);
    }

    static void showRoads() {
        System.out.println("Roads (km): R-A 2, A-B 3, B-C 2, R-C 4   (R = Restaurant)");
    }

    // ---------- Dijkstra's shortest path using a min-heap ----------
    static HashMap<String, Integer> dijkstra(String source) {
        HashMap<String, Integer> dist = new HashMap<>();
        for (String v : graph.keySet()) dist.put(v, Integer.MAX_VALUE);

        PriorityQueue<Node> pq = new PriorityQueue<>();
        dist.put(source, 0);
        pq.add(new Node(source, 0));

        while (!pq.isEmpty()) {
            Node cur = pq.poll();
            if (cur.dist > dist.get(cur.name)) continue;      // outdated entry
            for (Edge e : graph.get(cur.name)) {
                int nd = cur.dist + e.km;
                if (nd < dist.get(e.to)) {
                    dist.put(e.to, nd);
                    pq.add(new Node(e.to, nd));
                }
            }
        }
        return dist;
    }

    // ---------- Orders ----------
    static void addOrders() {
        System.out.println("Enter orders (example: O1 -> A, O2 -> B, O3 -> C)");
        System.out.print("Orders: ");
        String line = sc.nextLine().trim();
        if (line.toLowerCase().startsWith("orders:")) line = line.substring(7).trim();
        if (line.isEmpty()) {
            System.out.println("No orders entered.");
            return;
        }

        for (String part : line.split(",")) {
            String[] p = part.split("->");
            if (p.length != 2 || p[0].trim().isEmpty() || p[1].trim().isEmpty()) {
                System.out.println("Invalid format: \"" + part.trim() + "\". Use ID -> Location.");
                continue;
            }
            String id = p[0].trim();
            String loc = p[1].trim().toUpperCase();

            if (!graph.containsKey(loc)) {
                System.out.println("Location " + loc + " not found. Order " + id + " not added.");
            } else if (loc.equals(RESTAURANT)) {
                System.out.println(loc + " is the restaurant. Order " + id + " not added.");
            } else if (orderLocation.containsKey(id)) {
                System.out.println("Duplicate order " + id + ". Order not added.");
            } else {
                orderLocation.put(id, loc);
                ordersByLocation.computeIfAbsent(loc, k -> new ArrayList<>()).add(id);
                System.out.println("Order " + id + " added for location " + loc + ".");
            }
        }
    }

    static void showOrders() {
        if (ordersByLocation.isEmpty()) {
            System.out.println("No pending orders.");
            return;
        }
        for (String loc : new TreeSet<>(ordersByLocation.keySet())) {
            System.out.println(loc + ": " + String.join(", ", ordersByLocation.get(loc)));
        }
    }

    // ---------- Route planning (nearest pending stop first) ----------
    static void planRoute() {
        if (ordersByLocation.isEmpty()) {
            System.out.println("No pending orders.");
            return;
        }

        String current = RESTAURANT;
        int total = 0;
        StringBuilder route = new StringBuilder(RESTAURANT);
        List<String> delivered = new ArrayList<>();
        TreeSet<String> pending = new TreeSet<>(ordersByLocation.keySet());  // alphabetical order

        while (!pending.isEmpty()) {
            HashMap<String, Integer> dist = dijkstra(current);
            String next = null;
            int best = Integer.MAX_VALUE;
            for (String loc : pending) {              // strict '<' keeps alphabetical winner on ties
                if (dist.get(loc) < best) {
                    best = dist.get(loc);
                    next = loc;
                }
            }
            if (next == null) {
                System.out.println("Remaining locations are unreachable: " + pending);
                break;
            }
            total += best;
            route.append(" -> ").append(next);
            delivered.add("Delivered at " + next + ": " + String.join(", ", ordersByLocation.get(next)));
            for (String id : ordersByLocation.get(next)) orderLocation.remove(id);
            ordersByLocation.remove(next);
            pending.remove(next);
            current = next;
        }

        // return to the restaurant
        if (!current.equals(RESTAURANT)) {
            total += dijkstra(current).get(RESTAURANT);
            route.append(" -> ").append(RESTAURANT);
        }

        System.out.println("Route: " + route + " | " + total + " km");
        System.out.println("Total distance: " + total + " km");
        for (String d : delivered) System.out.println(d);
    }

    // ---------- Main ----------
    public static void main(String[] args) {
        buildMap();
        while (true) {
            System.out.println("\n--- Food Delivery Route Optimizer ---");
            System.out.println("1 Add Orders  2 Show Pending Orders  3 Plan Route  4 Show Roads  5 Exit");
            System.out.print("Choose option: ");
            String opt = sc.nextLine().trim();

            switch (opt) {
                case "1": addOrders(); break;
                case "2": showOrders(); break;
                case "3": planRoute(); break;
                case "4": showRoads(); break;
                case "5":
                    System.out.println("Thank you for using Food Delivery Route Optimizer!");
                    return;
                default: System.out.println("Invalid option. Choose 1-5.");
            }
        }
    }
}
