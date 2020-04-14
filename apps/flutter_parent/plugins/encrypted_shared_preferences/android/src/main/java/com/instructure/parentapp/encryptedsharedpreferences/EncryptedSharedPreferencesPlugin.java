// Copyright (C) 2020 - present Instructure, Inc.
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, version 3 of the License.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

package com.instructure.parentapp.encryptedsharedpreferences;

import android.content.Context;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;

/** SharedPreferencesPlugin */
public class EncryptedSharedPreferencesPlugin implements FlutterPlugin {
  private static final String CHANNEL_NAME = "com.instructure.parentapp/encrypted_shared_preferences";
  private MethodChannel channel;

  public static void registerWith(PluginRegistry.Registrar registrar) {
    final EncryptedSharedPreferencesPlugin plugin = new EncryptedSharedPreferencesPlugin();
    plugin.setupChannel(registrar.messenger(), registrar.context());
  }

  @Override
  public void onAttachedToEngine(FlutterPlugin.FlutterPluginBinding binding) {
    setupChannel(binding.getBinaryMessenger(), binding.getApplicationContext());
  }

  @Override
  public void onDetachedFromEngine(FlutterPlugin.FlutterPluginBinding binding) {
    teardownChannel();
  }

  private void setupChannel(BinaryMessenger messenger, Context context) {
    channel = new MethodChannel(messenger, CHANNEL_NAME);
    MethodCallHandlerImpl handler = new MethodCallHandlerImpl(context);
    channel.setMethodCallHandler(handler);
  }

  private void teardownChannel() {
    channel.setMethodCallHandler(null);
    channel = null;
  }
}
