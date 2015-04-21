/*
    Copyright (C) 2015   Jesper Zedlitz <jesper@zedlitz.de>
  
    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc.,
    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
  
*/
package tingeltangel;


import org.yaml.snakeyaml.Yaml;
import tingeltangel.core.Book;
import tingeltangel.core.Entry;
import tingeltangel.core.NoBookException;
import tingeltangel.core.Script;
import tingeltangel.tiptoireveng.Interpreter;
import tiptoi_reveng.lexer.Lexer;
import tiptoi_reveng.lexer.LexerException;
import tiptoi_reveng.node.Start;
import tiptoi_reveng.parser.Parser;
import tiptoi_reveng.parser.ParserException;

import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * Jeder Identifikator (YAML-Datei) muss eine OID bekommen.  Dabei aufpassen, welche OIDs manuell über ein zusätzliches Mapping vorgegeben wurde.
 * Außerdem muss jeder Dateiname eine OID bekommen. Die werden aber nicht nach außen angezeigt.
 * Jede Variable bekommt einen Register zugewiesen. Dabei muss man aufpassen, welche Register manuell verwendet wurden.
 */
public class ReadYamlFile {

    boolean ignoreAudioFiles = false;

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            exitPrintUsage();
        }

        File file = new File(args[0]);

        if (!file.exists()) {
            exitPrintUsage();
        }

        ReadYamlFile ryf = new ReadYamlFile();
        Book book = ryf.read(file);
        book.export(file.getParentFile());
    }

    private static void exitPrintUsage() {
        System.err.println("USAGE: ReadYamlFile <yaml-file>");
        System.exit(1);
    }

    private void convertOgg2Mp3(File oggFile, File mp3File) throws IOException {
        Process p = Runtime.getRuntime().exec("/usr/bin/avconv -i " + oggFile.getCanonicalPath() + " " + mp3File.getCanonicalPath());
        try {
            p.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Book read(File yamlFile) throws ParserException, IOException, LexerException, NoBookException {
        Yaml yaml = new Yaml();
        Map data = (Map) yaml.load(new FileInputStream(yamlFile));

        Map scripts = (Map) data.get("scripts");

        Interpreter interpreter = new Interpreter();
        File dir = yamlFile.getParentFile();
        Book book = new Book(null, dir);
        book.setID(8000 + ((Integer) data.get("product-id")));

        for (Object identifier : scripts.keySet()) {
            if (identifier instanceof Integer) {
                interpreter.getOids().add((Integer) identifier);
            } else {
                interpreter.addIdentifier(identifier.toString());
            }

            @SuppressWarnings("unchecked")
            List<String> commands = (List<String>) scripts.get(identifier);
            for (String command : commands) {
                PushbackReader reader = new PushbackReader(new StringReader(command));

                Lexer lexer = new Lexer(reader);
                Parser parser = new Parser(lexer);
                try {
                    Start start = parser.parse();
                    start.apply(interpreter);
                } catch (ParserException pe) {
                    System.err.println("Could not parse command " + command);
                    pe.printStackTrace();
                    throw pe;
                }
            }
        }

        interpreter.startSecondPhase();

        // Create Entry objects for sound files.
        for (String filename : interpreter.getFilename2oid().keySet()) {
            int oid = interpreter.getFilename2oid().get(filename);
            book.addEntry(oid);
            Entry entry = book.getEntryFromTingID(oid);

            if( !ignoreAudioFiles) {
                // Since the TipToi pen uses OggVorbis files we might have to convert the audio files to mp3.
                File mp3File = new File(dir, "audio_files/" + filename + ".mp3");
                if (!mp3File.exists()) {
                    File oggFile = new File(dir, "audio_files/" + filename + ".ogg");
                    if (!oggFile.exists()) {
                        throw new RuntimeException("Could not find audio file " + filename);
                    }
                    convertOgg2Mp3(oggFile, mp3File);
                }

                entry.setMP3(mp3File);
            }
        }


        for (Object identifier : scripts.keySet()) {
            int oid;
            if (identifier instanceof Integer) {
                oid = (Integer) identifier;
                if( oid < 15000) {
                    oid += 7000;
                }
            } else {
                oid = interpreter.getIdentifier2oid().get(identifier.toString());
            }

            @SuppressWarnings("unchecked")
            List<String> commands = (List<String>) scripts.get(identifier);
            for (String command : commands) {
                PushbackReader reader = new PushbackReader(new StringReader(command));

                Lexer lexer = new Lexer(reader);
                Parser parser = new Parser(lexer);
                try {
                    Start start = parser.parse();
                    start.apply(interpreter);
                } catch (ParserException pe) {
                    System.err.println("Could not parse command " + command);
                    pe.printStackTrace();
                    throw pe;
                }
            }

            interpreter.getScript().append("end\n");

            book.addEntry(oid);
            Entry entry = book.getEntryFromTingID(oid);
            Script script = new Script(interpreter.getScript().toString(), entry);
            entry.setScript(script);
            entry.setHint(identifier.toString());
        }

        return book;
    }

}