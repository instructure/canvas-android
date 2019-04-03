/*
 * Copyright (C) 2016 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.instructure.student.util;

public class HelpDeskUtility {
	public static boolean hasHelpDeskSupport(String domain)
	{
		//Handle the logged out case.
		if(domain == null || domain.equals(""))
			return false;
		
		String[] approved = {
                "mobiledev.instructure.com",
                "mobileqa.instructure.com",
                "training.instructure.com",

                "alamo.instructure.com",
                "ace.instructure.com",
                "ai.instructure.com",
                "cwu.instructure.com",
                "champlain.instructure.com",
                "chipola.instructure.com",
                "columbiasce.instructure.com",
                "dts.instructure.com",
                "eaglegatecollege.instructure.com",
                "necb.instructure.com",
                "emmanuel.instructure.com",
                "houstonisd.instructure.com",
                "jesuit.instructure.com",
                "midlandu.instructure.com",
                "montclair.instructure.com",
                "nsc.instructure.com",
                "pcc.instructure.com",
                "pinnacle.instructure.com",
                "playfullearning.instructure.com",
                "rider.instructure.com",
                "rivier.instructure.com",
                "salemstate.instructure.com",
                "samuelmerritt.instructure.com",
                "sjsu.instructure.com",
                "sofia.instructure.com",
                "unex.instructure.com",
                "cole2.instructure.com",
                "umd.instructure.com",
                "umdearborn.instructure.com",
                "usu.instructure.com",
                "wnc.instructure.com",
                "wnmu.instructure.com",

                "coahomacc.instructure.com",
                "colin.instructure.com",
                "eccc.instructure.com",
                "eastms.instructure.com",
                "hindscc.instructure.com",
                "holmescc.instructure.com",
                "iccms.instructure.com",
                "jcjc.instructure.com",
                "meridiancc.instructure.com",
                "msdelta.instructure.com",
                "mgccc.instructure.com",
                "nemcc.instructure.com",
                "northwestms.instructure.com",
                "prcc.instructure.com",
                "smcc.instructure.com",
                "mccb.instructure.com"
		};
		
		for(int i = 0; i < approved.length; i++)
		{
			if(domain.contains(approved[i]))
				return true;
		}
		
		return false;
	}
}
