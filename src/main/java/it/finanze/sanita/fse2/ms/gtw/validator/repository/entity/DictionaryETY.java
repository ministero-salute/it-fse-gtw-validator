/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.validator.repository.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;


/**
 * Model to save dictionary.
 */
@Document(collection = "#{@dictionaryBean}")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DictionaryETY {

    public static final String FIELD_SYSTEM = "system";
    public static final String FIELD_VERSION = "version";
	public static final String FIELD_CREATION_UPDATE = "creation_date";
    public static final String FIELD_RELEASE_DATE = "release_date";
    public static final String FIELD_WHITELIST = "whitelist";

	@Id
	private String id;
	@Field(name = FIELD_SYSTEM)
	private String system;
	@Field(name = FIELD_VERSION)
	private String version;
	@Field(name = FIELD_CREATION_UPDATE)
    private Date creationDate;
    @Field(name = FIELD_RELEASE_DATE)
    private Date releaseDate;
    @Field(name = FIELD_WHITELIST)
    private boolean whiteList;
	 
}