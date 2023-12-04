package mytest;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 参考：https://github.com/aragozin/heaplib 下的TextTable
 * 思路：先找出每列最大宽度，然后每行数据添加空格padding
 * 注意: 不支持如下这种：
 * <pre>
 * app           /hermes-center/health
 *               /app/config
 *               /app/jars
 *               /app/health
 *               /app/info
 *               /app/ports
 * jvm           /jvm/gc/count
 *               /jvm/heap/used
 * </pre>
 * @author bairen
 * @description
 **/
public class TextTable {

    private List<String[]> rows = new ArrayList<String[]>();
    private int colCount;

    public TextTable() {
    }

    public TextTable(String... header) {
        this.addRow(header);
    }

    public void addRow(List<String> row) {
        addRow(row.toArray(new String[row.size()]));
    }
    public void addRow(String... row) {
        if (rows.size() == 0) {
            colCount = row.length;
        }
        if (row.length > colCount) {
            throw new IllegalArgumentException("Row is longer than table");
        }
        rows.add(Arrays.copyOf(row, colCount));
    }

    public String toString() {
        return formatTextTable(200);
    }

    public String formatTextTable(int maxCellWidth) {
        return formatTable(rows, maxCellWidth, true);
    }

    public String formatTextTableUnbordered(int maxCellWidth) {
        return formatTable(rows, maxCellWidth, false);
    }

    private String formatTable(List<String[]> content, int maxCell, boolean table) {
        //保存每列的宽度
        int[] width = new int[content.get(0).length];
        for (String[] row : content) {
            for (int i = 0; i < row.length; ++i) {
                width[i] = Math.min(Math.max(width[i], row[i] == null ? 0 : row[i].length()), maxCell);
            }
        }
        StringBuffer sb = new StringBuffer();
        boolean header = table;
        for (String[] row : content) {
            for (int i = 0; i != row.length; ++i) {
                String cell = row[i] == null ? "" : row[i];
                //超过宽度截断
                if (cell.length() > width[i]) {
                    cell = cell.substring(0, width[i] - 3) + "...";
                }
                sb.append(cell);
                //padding
                for (int j = 0; j < width[i] - cell.length(); ++j) {
                    sb.append(' ');
                }
                sb.append('|');
            }
            sb.append('\n');
            //增加表头分割
            if (header) {
                header = false;
                for (int n : width) {
                    for (int i = 0; i != n; ++i) {
                        sb.append('-');
                    }
                    sb.append('|');
                }
                sb.append('\n');
            }
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        TextTable table = new TextTable("cmd","count","in","out");
        table.addRow("cluster", "1", "81B", "345B");
        table.addRow("hgetall", "43", "5.4KB", "695.8KB");
        table.addRow("ping", "43", "0B", "671B");
        table.addRow("setnx", "1223", "184.1KB", "14.0MB");
        table.addRow("replconf", "1692", "1.7KB", "255.5KB");

        System.out.println(table.formatTextTable(100));


    }
}