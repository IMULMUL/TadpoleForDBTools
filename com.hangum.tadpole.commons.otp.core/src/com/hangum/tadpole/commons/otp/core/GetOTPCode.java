/*******************************************************************************
 * Copyright (c) 2016 hangum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     hangum - initial API and implementation
 ******************************************************************************/
package com.hangum.tadpole.commons.otp.core;

import com.hangum.tadpole.commons.otp.OTPAuthManager;

/**
 * Get otp code
 * 
 * @author hangum
 *
 */
public class GetOTPCode {

	/**
	 * otp validate
	 *  
	 * @param userID
	 * @param secretKEY
	 * @param otpCode
	 * @throws Exception
	 */
	public static void isValidate(String userID, String secretKEY, String otpCode) throws Exception{
		OTPAuthManager.getInstance().isValidate(userID, secretKEY, otpCode);
	}
	
	/**
	 * get secret key
	 * @return
	 */
	public static String getSecretKey() {
		return OTPAuthManager.getInstance().getSecretKey();
	}
	
	/**
	 * get url
	 * 
	 * @param user
	 * @param host
	 * @param secret
	 * @return
	 */
	public static String getURL(String user, String host, String secret) {
		return OTPAuthManager.getInstance().getURL(user, host, secret);
	}
	
}
