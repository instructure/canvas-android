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

package com.instructure.student.lazyloader;

import java.io.File;

import android.content.Context;

public class FileCache {

	private File cacheDir;

	public FileCache(Context context){
		//Find the dir to save cached images
		if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
			cacheDir=new File(android.os.Environment.getExternalStorageDirectory(),"Candroid");
		else
			cacheDir=context.getCacheDir();
		if(!cacheDir.exists())
			cacheDir.mkdirs();
	}

	public File getFile(String url){
		//I identify images by hashcode. Not a perfect solution, good for the demo.
		String filename=String.valueOf(url.hashCode());
		//Another possible solution (thanks to grantland)
		//String filename = URLEncoder.encode(url);
		File f = new File(cacheDir, filename);
		return f;

	}

	public void clear(){
		File[] files=cacheDir.listFiles();
		if(files==null)
			return;
		for(File f:files)
			f.delete();
	}

}
