/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.validator.logging;

public interface FailedDeliveryCallback<E> {
	
    void onFailedDelivery(E evt, Throwable throwable);
    
}