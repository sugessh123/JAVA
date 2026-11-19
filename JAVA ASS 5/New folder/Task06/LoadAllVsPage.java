import javax.swing.table.DefaultTableModel;
import java.util.*;

// Measures the cost of putting 100,000 registrations into a table model at once, compared with
// one page of 50 rows. It uses an in-memory list, so it measures table-model time and memory only;
// network and database time would come on top for a real MySQL server.
public class LoadAllVsPage {
    static long usedMb() {
        System.gc(); System.gc();
        Runtime r = Runtime.getRuntime();
        return (r.totalMemory() - r.freeMemory()) / (1024 * 1024);
    }

    public static void main(String[] args) {
        List<Registration> source = new ArrayList<>();
        for (int i = 1; i <= 100_000; i++) source.add(new Registration(i, "CSA" + (2400000 + i), "Student " + i, "Hackathon", "2026-11-15 10:00:00"));
        String[] cols = { "ID", "Student ID", "Name", "Event", "Registered at" };

        long before = usedMb();
        long t0 = System.nanoTime();
        DefaultTableModel all = new DefaultTableModel(cols, 0);
        for (Registration r : source) all.addRow(new Object[] { r.id, r.studentId, r.studentName, r.eventName, r.registeredAt });
        long allMs = (System.nanoTime() - t0) / 1_000_000;
        long allMb = usedMb() - before;
        System.out.println("Load all 100,000 rows : " + allMs + " ms, about " + allMb + " MB extra heap, rows in table = " + all.getRowCount());
        all = null;

        before = usedMb();
        t0 = System.nanoTime();
        DefaultTableModel page = new DefaultTableModel(cols, 0);
        for (Registration r : source.subList(0, 50)) page.addRow(new Object[] { r.id, r.studentId, r.studentName, r.eventName, r.registeredAt });
        double pageMs = (System.nanoTime() - t0) / 1e6;
        long pageKb = Math.max(0, usedMb() - before);
        System.out.println("Load one page of 50   : " + String.format("%.2f", pageMs) + " ms, about " + pageKb + " MB extra heap, rows in table = " + page.getRowCount());
    }
}
