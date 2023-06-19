/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.validator.enums;

import org.apache.commons.lang3.StringUtils;

public enum SystemTypeEnum {

    TS("TS"),
    NONE(null);

    private final String name;

    SystemTypeEnum(String name) {
        this.name = name;
    }

    public String value() {
        return name;
    }

    public static SystemTypeEnum of(String system) {
        SystemTypeEnum res = SystemTypeEnum.NONE;
        if(!StringUtils.isBlank(system) && system.equalsIgnoreCase(TS.name)) {
            res = SystemTypeEnum.TS;
        }
        return res;
    }
}
