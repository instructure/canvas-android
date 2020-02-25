/*
 * Copyright (C) 2020 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import 'package:fluro/fluro.dart';
import 'package:flutter/material.dart';
import 'package:flutter_parent/parent_app.dart';
import 'package:flutter_parent/screens/inbox/conversation_list/conversation_list_screen.dart';

class ParentRouter {
  static final Router router = Router();

  static bool _isInitialized = false;

  static final String root = '/';
  static final String conversations = '/conversations';

  static void init() {
    if (!_isInitialized) {
      _isInitialized = true;
      router.define(root, handler: _rootHandler);
      router.define(conversations, handler: _conversationsHandler);
    }
  }

  // Handlers
  static Handler _rootHandler = Handler(handlerFunc: (BuildContext context, Map<String, List<String>> params) {
    return ParentApp();
  });

  static Handler _conversationsHandler = Handler(handlerFunc: (BuildContext context, Map<String, List<String>> params) {
    return ConversationListScreen();
  });
}
