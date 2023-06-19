/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.validator.base;

import java.nio.file.Path;
import java.nio.file.Paths;

public enum SchematronPath {
    BASE_PATH(Paths.get("src", "test", "resources", "Files")),
    FSE(Paths.get(BASE_PATH.toString(), "schematronFSE")),
    LDO(Paths.get(BASE_PATH.toString(), "schematronLDO")),
    PSS(Paths.get(BASE_PATH.toString(), "schematronPSS")),
    RAD(Paths.get(BASE_PATH.toString(), "schematronRAD")),
    RSA(Paths.get(BASE_PATH.toString(), "schematronRSA")),
    SIN_VAC(Paths.get(BASE_PATH.toString(), "schematronSinVACC")),
    VPS(Paths.get(BASE_PATH.toString(), "schematronVPS")),
    VAC(Paths.get(BASE_PATH.toString(), "schematronVACC"));

    private final Path path;

    SchematronPath(Path path) {
        this.path = path;
    }

    public String OK() {
        return Paths.get(path.toString(), "OK").toString();
    }

    public String KO() {
        return Paths.get(path.toString(), "KO").toString();
    }

    public String ERROR() {
        return Paths.get(path.toString(), "ERROR").toString();
    }
}
