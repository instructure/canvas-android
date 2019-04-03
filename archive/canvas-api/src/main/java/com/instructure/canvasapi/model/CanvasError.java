/*
 * Copyright (C) 2017 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.instructure.canvasapi.model;

public class CanvasError {
    private String status;
    private Error error;
    private String message;
    private String formattedStatus;
    private String errorMessage;

    public static CanvasError createError(String status, String message) {
        CanvasError error = new CanvasError();
        error.status = status;
        error.message = message;

        return error;
    }

    public String getStatus(){
        if (formattedStatus == null) {
            if (status != null && status.length() > 1) {
                formattedStatus = status.substring(0, 1).toUpperCase() + status.substring(1);
            } else {
                formattedStatus = "";
            }
        }
        return formattedStatus;
    }

    public String getMessage(){
        if(message != null){
            return message;
        }

        return "";
    }

    public Error getError(){
        return error;
    }

    @Override
    public String toString() {
        if (errorMessage == null) {
            if (error != null) {
                errorMessage = getError().getMessage();
            } else {
                errorMessage = getMessage();
            }

            if (getStatus().length() > 0) {
                errorMessage = getStatus() + ": " + errorMessage;
            }
        }

        return errorMessage;
    }


    public class Error {
        private String message;
        private String formattedMessage;

        public String getMessage() {
            if (formattedMessage == null) {
                if (message != null && message.length() > 1) {
                    formattedMessage = message.substring(0, 1).toUpperCase() + message.substring(1) + ".";
                } else {
                    formattedMessage = "";
                }
            }
            return formattedMessage;
        }


        @Override
        public String toString() {
            return getMessage();
        }
    }
}
