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

import 'package:flutter/material.dart';
import 'package:flutter_parent/models/attachment.dart';
import 'package:flutter_parent/screens/inbox/attachment_utils/attachment_extensions.dart';
import 'package:flutter_parent/utils/design/parent_colors.dart';
import 'package:transparent_image/transparent_image.dart';

class AttachmentIndicatorWidget extends StatelessWidget {
  final Attachment attachment;
  final Function(Attachment)? onAttachmentClicked;

  const AttachmentIndicatorWidget({required this.attachment, required this.onAttachmentClicked, super.key});

  @override
  Widget build(BuildContext context) {
    return MergeSemantics(
      child: Container(
        key: Key('attachment-${attachment.id}'),
        width: 112,
        height: 96,
        child: Stack(
          alignment: Alignment.center,
          children: [
            Container(
              decoration: BoxDecoration(
                border: Border.all(color: ParentColors.tiara, width: 0.5),
                borderRadius: BorderRadius.all(Radius.circular(4)),
              ),
            ),
            Stack(
              fit: StackFit.expand,
              children: [
                Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    Icon(
                      attachment.getIcon(),
                      color: Theme.of(context).colorScheme.secondary,
                    ),
                    Padding(
                      padding: const EdgeInsets.fromLTRB(12, 11, 12, 0),
                      child: Text(
                        attachment.displayName!,
                        maxLines: 2,
                        overflow: TextOverflow.ellipsis,
                        textAlign: TextAlign.center,
                        style: TextStyle(fontSize: 12, fontWeight: FontWeight.w500),
                      ),
                    )
                  ],
                ),
                if (attachment.thumbnailUrl != null && attachment.thumbnailUrl!.isNotEmpty)
                  ClipRRect(
                    borderRadius: new BorderRadius.circular(4),
                    child: FadeInImage.memoryNetwork(
                      fadeInDuration: const Duration(milliseconds: 300),
                      fit: BoxFit.cover,
                      image: attachment.thumbnailUrl!,
                      placeholder: kTransparentImage,
                    ),
                  ),
                Material(
                  color: Colors.transparent,
                  child: InkWell(
                    onTap: () {
                      if (onAttachmentClicked != null) onAttachmentClicked!(attachment);
                    },
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
