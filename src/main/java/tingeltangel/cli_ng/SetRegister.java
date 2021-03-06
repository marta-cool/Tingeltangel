/*
 * Copyright 2016 mdames.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tingeltangel.cli_ng;

/**
 *
 * @author mdames
 */
class SetRegister extends CliCmd {

    @Override
    String getName() {
        return("set-register");
    }

    @Override
    String getDescription() {
        return("set-register <#> <value>");
    }

    @Override
    int execute(String[] args) {
        if(args.length != 2) {
            return(error("falsche Anzahl von Parametern"));
        }
        int r;
        try {
            r = Integer.parseInt(args[0]);
        } catch(NumberFormatException e) {
            return(error("Register ist keine Zahl zw. 0 und 99"));
        }
        if(r < 0 || r > 99) {
            return(error("Register keine Zahl zw. 0 und 99"));
        }
        int v;
        try {
            v = Integer.parseInt(args[1]);
        } catch(NumberFormatException e) {
            return(error("Register ist keine Zahl zw. 0 und 0xffff"));
        }
        if(v < 0 || v > 0xffff) {
            return(error("Value keine Zahl zw. 0 und 0xffff"));
        }
        
        CLI.getBook().getEmulator().setRegister(r, v);
        return(ok());
    }

}
