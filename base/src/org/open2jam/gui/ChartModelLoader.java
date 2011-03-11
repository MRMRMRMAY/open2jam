package org.open2jam.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.zip.CRC32;
import javax.swing.SwingWorker;
import org.open2jam.parser.ChartList;
import org.open2jam.parser.ChartParser;

/**
 *
 * @author fox
 */
public class ChartModelLoader extends SwingWorker<ChartListTableModel,ChartList>
{
    static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    private ChartListTableModel table_model;
    private File dir;

    public ChartModelLoader(ChartListTableModel table_model, File dir){
        this.table_model = table_model;
        this.dir = dir;
    }

    protected ChartListTableModel doInBackground() {
        try{
        table_model.clear();
        ArrayList<File> files = new ArrayList<File>(Arrays.asList(dir.listFiles()));
        double perc = files.size() / 100d;
        for(int i=0;i<files.size();i++)
        {
            ChartList cl = ChartParser.parseFile(files.get(i));
            if(cl != null)publish(cl);
            else if(files.get(i).isDirectory()){
                List<File> nl = Arrays.asList(files.get(i).listFiles());
                files.addAll(nl);
                perc = files.size() / 100d;
            }
            setProgress((int)(i/perc));
        }
        setProgress(100);
        return table_model;
        }catch(Exception e){
            logger.log(Level.SEVERE, "Exception in chart loader ! {0} {1}", new Object[]{e.toString(), e.getMessage()});
            System.exit(1);
            return null;
        }
    }

    @Override
    protected void done() {
        //let's make a crc32 hash for the cache name
        CRC32 cs = new CRC32();
        cs.reset();

        byte[] d = dir.toString().getBytes();
        cs.update(d, 0, d.length);

        String str = "cache_"+Long.toHexString(cs.getValue()).toUpperCase()+".obj";
        try {
            new ObjectOutputStream(new FileOutputStream(str)).writeObject(table_model.getRawList());
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "{0}", ex);
        }
    }

    @Override
     protected void process(List<ChartList> chunks) {
         for (ChartList row : chunks) {
             table_model.addRow(row);
         }
     }
}
