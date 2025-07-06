package ru.shift.Haslavskiy_DU;

import ru.shift.Haslavskiy_DU.filemanager.FileManager;

/**
 * Hello world!
 *
 */
public class Main
{
    public static void main( String[] args )
    {
        FileManager fileManager = new FileManager(args);
        fileManager.process();
    }
}
