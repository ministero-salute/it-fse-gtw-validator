
/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (C) 2023 Ministero della Salute
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
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
