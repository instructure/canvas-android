/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
 */
package com.instructure.horizon.horizonui.showroom.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.organisms.inputs.datepicker.DatePicker
import com.instructure.horizon.horizonui.organisms.inputs.datepicker.DatePickerInputSize
import com.instructure.horizon.horizonui.organisms.inputs.datepicker.DatePickerState
import com.instructure.horizon.horizonui.organisms.inputs.multiselect.MultiSelect
import com.instructure.horizon.horizonui.organisms.inputs.multiselect.MultiSelectInputSize
import com.instructure.horizon.horizonui.organisms.inputs.multiselect.MultiSelectState
import com.instructure.horizon.horizonui.organisms.inputs.numberfield.NumberField
import com.instructure.horizon.horizonui.organisms.inputs.numberfield.NumberFieldInputSize
import com.instructure.horizon.horizonui.organisms.inputs.numberfield.NumberFieldState
import com.instructure.horizon.horizonui.organisms.inputs.singleselect.SingleSelect
import com.instructure.horizon.horizonui.organisms.inputs.singleselect.SingleSelectInputSize
import com.instructure.horizon.horizonui.organisms.inputs.singleselect.SingleSelectState
import com.instructure.horizon.horizonui.organisms.inputs.textarea.TextArea
import com.instructure.horizon.horizonui.organisms.inputs.textarea.TextAreaState
import com.instructure.horizon.horizonui.organisms.inputs.textfield.TextField
import com.instructure.horizon.horizonui.organisms.inputs.textfield.TextFieldInputSize
import com.instructure.horizon.horizonui.organisms.inputs.textfield.TextFieldState
import com.instructure.horizon.horizonui.organisms.inputs.timepicker.TimePicker
import com.instructure.horizon.horizonui.organisms.inputs.timepicker.TimePickerInputSize
import com.instructure.horizon.horizonui.organisms.inputs.timepicker.TimePickerState
import java.time.LocalTime
import java.util.Date

@Composable
fun InputsScreen() {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        SingleSelectShowcase()

        MultiSelectShowcase()

        DatePickerShowcase()

        TimePickerShowcase()

        TextFieldShowcase()

        TextAreaShowcase()

        NumberFieldShowcase()
    }
}

@Composable
private fun SingleSelectShowcase() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Single Select SMALL", style = HorizonTypography.h2)
        var selectedValueSmall by remember { mutableStateOf<String?>(null) }
        var isOpenSmall by remember { mutableStateOf(false) }
        var errorTextSmall by remember { mutableStateOf<String?>(null) }
        val stateSmall = SingleSelectState(
            label = "Single Select",
            placeHolderText = "Select a value",
            size = SingleSelectInputSize.Small,
            options = listOf("Value", "[Error]", "[Clear]"),
            isMenuOpen = isOpenSmall,
            selectedOption = selectedValueSmall,
            onOptionSelected = {
                errorTextSmall = if (it == "[Error]") {
                    "This is an error message"
                } else {
                    null
                }
                selectedValueSmall = if (it == "[Clear]") null else it
            },
            onMenuOpenChanged = { isOpenSmall = it },
            errorText = errorTextSmall,
            helperText = "This is a Single Select from the Android Design System",
        )

        SingleSelect(stateSmall)

        Text("Single Select MEDIUM", style = HorizonTypography.h2)
        var selectedValueMedium by remember { mutableStateOf<String?>(null) }
        var isOpenMedium by remember { mutableStateOf(false) }
        var errorTextMedium by remember { mutableStateOf<String?>(null) }
        val stateMedium = SingleSelectState(
            label = "Single Select",
            placeHolderText = "Select a value",
            size = SingleSelectInputSize.Medium,
            options = listOf("Value", "[Error]", "[Clear]"),
            isMenuOpen = isOpenMedium,
            selectedOption = selectedValueMedium,
            onOptionSelected = {
                errorTextMedium = if (it == "[Error]") {
                    "This is an error message"
                } else {
                    null
                }
                selectedValueMedium = if (it == "[Clear]") null else it
            },
            errorText = errorTextMedium,
            onMenuOpenChanged = { isOpenMedium = it },
            helperText = "This is a Single Select from the Android Design System",
        )

        SingleSelect(stateMedium)
    }
}

@Composable
private fun MultiSelectShowcase() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Multi Select SMALL", style = HorizonTypography.h2)
        var selectedValuesSmall by remember { mutableStateOf(emptyList<String>()) }
        var isOpenSmall by remember { mutableStateOf(false) }
        var errorTextSmall by remember { mutableStateOf<String?>(null) }
        val stateSmall = MultiSelectState(
            label = "Multi Selects",
            placeHolderText = "Select values",
            size = MultiSelectInputSize.Small,
            options = listOf("Value 1", "Value 2", "[Error]", "[Clear]"),
            isMenuOpen = isOpenSmall,
            selectedOptions = selectedValuesSmall,
            onOptionSelected = {
                errorTextSmall = if (it == "[Error]") {
                    "This is an error message"
                } else {
                    null
                }
                selectedValuesSmall = if (it == "[Clear]") emptyList() else selectedValuesSmall + it
            },
            onOptionRemoved = {
                selectedValuesSmall = selectedValuesSmall - it
            },
            onMenuOpenChanged = { isOpenSmall = it },
            errorText = errorTextSmall,
            helperText = "This is a Single Select from the Android Design System",
        )

        MultiSelect(stateSmall)

        Text("Multi Selects MEDIUM", style = HorizonTypography.h2)
        var selectedValuesMedium by remember { mutableStateOf(listOf<String>()) }
        var isOpenMedium by remember { mutableStateOf(false) }
        var errorTextMedium by remember { mutableStateOf<String?>(null) }
        val stateMedium = MultiSelectState(
            label = "Multi Select",
            placeHolderText = "Select values",
            size = MultiSelectInputSize.Medium,
            options = listOf("1Value 1", "Value 2", "[Error]", "[Clear]"),
            isMenuOpen = isOpenMedium,
            selectedOptions = selectedValuesMedium,
            onOptionSelected = {
                errorTextMedium = if (it == "[Error]") {
                    "This is an error message"
                } else {
                    null
                }
                selectedValuesMedium = if (it == "[Clear]") emptyList() else selectedValuesMedium + it
            },
            onOptionRemoved = {
                selectedValuesMedium = selectedValuesMedium - it
            },
            errorText = errorTextMedium,
            onMenuOpenChanged = { isOpenMedium = it },
            helperText = "This is a Single Select from the Android Design System",
        )

        MultiSelect(stateMedium)
    }
}

@Composable
private fun DatePickerShowcase() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Date Pickers SMALL", style = HorizonTypography.h2)
            val state = DatePickerState(
                size = DatePickerInputSize.Small,
                helperText = "This is a Date Picker from the Android Design System",
            )

            DatePicker(
                state.copy(
                    label = "Date Picker PLACEHOLDER",
                    placeHolderText = "Select a date"
                )
            )

            DatePicker(state.copy(label = "Date Picker FOCUSED", isFocused = true, selectedDate = Date()))

            DatePicker(state.copy(label = "Date Picker ERROR", errorText = "This is an error message", selectedDate = Date()))

            DatePicker(state.copy(label = "Date Picker DISABLED", enabled = false, selectedDate = Date()))
        }

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Date Pickers MEDIUM", style = HorizonTypography.h2)
            val state = DatePickerState(
                size = DatePickerInputSize.Medium,
                helperText = "This is a Date Picker from the Android Design System",
            )

            DatePicker(state.copy(label = "Date Picker PLACEHOLDER", placeHolderText = "Select a date"))

            DatePicker(state.copy(label = "Date Picker FOCUSED", isFocused = true,selectedDate = Date()))

            DatePicker(state.copy(label = "Date Picker ERROR", errorText = "This is an error message", selectedDate = Date()))

            DatePicker(state.copy(label = "Date Picker DISABLED", enabled = false, selectedDate = Date()))
        }
    }
}

@Composable
private fun TimePickerShowcase() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Time Pickers SMALL", style = HorizonTypography.h2)
            val state = TimePickerState(
                size = TimePickerInputSize.Small,
                helperText = "This is a Time Picker from the Android Design System",
            )

            TimePicker(state.copy(label = "Time PickerPLACEHOLDER", placeHolderText = "Select a time"))

            TimePicker(state.copy(label = "Time Picker FOCUSED", isFocused = true, selectedTime = LocalTime.now()))

            TimePicker(state.copy(label = "Time Picker ERROR",  isFocused = true, errorText = "This is an error message", selectedTime = LocalTime.now()))

            TimePicker(state.copy(label = "Time Picker DISABLED", enabled = false, selectedTime = LocalTime.now()))
        }

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Time Pickers MEDIUM", style = HorizonTypography.h2)
            val state = TimePickerState(
                size = TimePickerInputSize.Medium,
                helperText = "This is a Time Picker from the Android Design System",
            )

            TimePicker(state.copy(label = "Time Picker PLACEHOLDER", placeHolderText = "Select a time"))

            TimePicker(state.copy(label = "Time Picker FOCUSED", isFocused = true, selectedTime = LocalTime.now()))

            TimePicker(state.copy(label = "Time Picker ERROR", isFocused = true, errorText = "This is an error message", selectedTime = LocalTime.now()))

            TimePicker(state.copy(label = "Time Picker DISABLED", enabled = false, selectedTime = LocalTime.now()))
        }
    }
}

@Composable
private fun TextAreaShowcase() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Text Area", style = HorizonTypography.h2)
        val modifier = Modifier
            .defaultMinSize(minHeight = 100.dp)
        var value by remember { mutableStateOf(TextFieldValue("")) }
        var isFocused by remember { mutableStateOf(false) }
        var error by remember { mutableStateOf<String?>(null) }
        val state = TextAreaState(
            label = "Text Area",
            placeHolderText = "Type 'error' to see the error state",
            value = value,
            onValueChange = {
                value = it
                error = if (it.text.contains("error")) {
                    "This is an error message"
                } else {
                    null
                }
            },
            errorText = error,
            isFocused = isFocused,
            onFocusChanged = { isFocused = it },
            helperText = "This is a Time Picker from the Android Design System",
        )

        TextArea(state, modifier)
    }
}

@Composable
private fun TextFieldShowcase() {
    Column (verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Text Field SMALL", style = HorizonTypography.h2)
        var valueSmall by remember { mutableStateOf(TextFieldValue("")) }
        var isFocusedSmall by remember { mutableStateOf(false) }
        var errorSmall by remember { mutableStateOf<String?>(null) }
        val stateSmall = TextFieldState(
            label = "Text Field",
            size = TextFieldInputSize.Small,
            placeHolderText = "Type 'error' to see the error state",
            value = valueSmall,
            onValueChange = {
                valueSmall = it
                errorSmall = if (it.text.contains("error")) {
                    "This is an error message"
                } else {
                    null
                }
            },
            errorText = errorSmall,
            isFocused = isFocusedSmall,
            onFocusChanged = { isFocusedSmall = it },
            helperText = "This is a Time Picker from the Android Design System"
        )

        TextField(stateSmall)

        Text("Text Field MEDIUM", style = HorizonTypography.h2)
        var valueMedium by remember { mutableStateOf(TextFieldValue("")) }
        var isFocusedMedium by remember { mutableStateOf(false) }
        var errorMedium by remember { mutableStateOf<String?>(null) }
        val stateMedium = TextFieldState(
            label = "Text Field",
            size = TextFieldInputSize.Medium,
            placeHolderText = "Type 'error' to see the error state",
            value = valueMedium,
            onValueChange = {
                valueMedium = it
                errorMedium = if (it.text.contains("error")) {
                    "This is an error message"
                } else {
                    null
                }
            },
            errorText = errorMedium,
            isFocused = isFocusedMedium,
            onFocusChanged = { isFocusedMedium = it },
            helperText = "This is a Time Picker from the Android Design System"
        )

        TextField(stateMedium)
    }
}

@Composable
private fun NumberFieldShowcase() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Number Field SMALL", style = HorizonTypography.h2)
            var valueSmall by remember { mutableStateOf(TextFieldValue("")) }
            var isFocusedSmall by remember { mutableStateOf(false) }
            var errorSmall by remember { mutableStateOf<String?>(null) }
            val stateSmall = NumberFieldState(
                label = "Number Field",
                size = NumberFieldInputSize.Small,
                placeHolderText = "Type 'NaN' to see the error state",
                value = valueSmall,
                onValueChange = {
                    valueSmall = it
                    errorSmall = if (it.text.toIntOrNull() == null) {
                        "This is an error message"
                    } else {
                        null
                    }
                },
                errorText = errorSmall,
                isFocused = isFocusedSmall,
                onFocusChanged = { isFocusedSmall = it },
                helperText = "This is a Number Field from the Android Design System",
            )

            NumberField(stateSmall)


            Text("Number Field MEDIUM", style = HorizonTypography.h2)
            var valueMedium by remember { mutableStateOf(TextFieldValue("")) }
            var isFocusedMedium by remember { mutableStateOf(false) }
            var errorMedium by remember { mutableStateOf<String?>(null) }
            val stateMedium = NumberFieldState(
                label = "Number Field",
                size = NumberFieldInputSize.Medium,
                placeHolderText = "Type 'NaN' to see the error state",
                value = valueMedium,
                onValueChange = {
                    valueMedium = it
                    errorMedium = if (it.text.toIntOrNull() == null) {
                        "This is an error message"
                    } else {
                        null
                    }
                },
                errorText = errorMedium,
                isFocused = isFocusedMedium,
                onFocusChanged = { isFocusedMedium = it },
                helperText = "This is a Number Field from the Android Design System",
            )

            NumberField(stateMedium)
        }

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Number Field SMALL", style = HorizonTypography.h2)
            var valueSmall by remember { mutableStateOf(TextFieldValue("")) }
            var isFocusedSmall by remember { mutableStateOf(false) }
            var errorSmall by remember { mutableStateOf<String?>(null) }
            val stateSmall = NumberFieldState(
                label = "Number Field",
                size = NumberFieldInputSize.Small,
                placeHolderText = "Type 'NaN' to see the error state",
                value = valueSmall,
                onValueChange = {
                    valueSmall = it
                    errorSmall = if (it.text.toIntOrNull() == null) {
                        "This is an error message"
                    } else {
                        null
                    }
                },
                showIncreaseDecreaseButtons = true,
                onIncreaseButtonClick = {
                    valueSmall = TextFieldValue(((valueSmall.text.toIntOrNull() ?: 0) + 1).toString())
                },
                onDecreaseButtonClick = {
                    valueSmall = TextFieldValue(((valueSmall.text.toIntOrNull() ?: 0) - 1).toString())
                },
                errorText = errorSmall,
                isFocused = isFocusedSmall,
                onFocusChanged = { isFocusedSmall = it },
                helperText = "This is a Number Field from the Android Design System",
            )

            NumberField(stateSmall)


            Text("Number Field MEDIUM", style = HorizonTypography.h2)
            var valueMedium by remember { mutableStateOf(TextFieldValue("")) }
            var isFocusedMedium by remember { mutableStateOf(false) }
            var errorMedium by remember { mutableStateOf<String?>(null) }
            val stateMedium = NumberFieldState(
                label = "Number Field",
                size = NumberFieldInputSize.Medium,
                placeHolderText = "Type 'NaN' to see the error state",
                value = valueMedium,
                onValueChange = {
                    valueMedium = it
                    errorMedium = if (it.text.toIntOrNull() == null) {
                        "This is an error message"
                    } else {
                        null
                    }
                },
                showIncreaseDecreaseButtons = true,
                onIncreaseButtonClick = {
                    valueMedium = TextFieldValue(((valueMedium.text.toIntOrNull() ?: 0) + 1).toString())
                },
                onDecreaseButtonClick = {
                    valueMedium = TextFieldValue(((valueMedium.text.toIntOrNull() ?: 0) - 1).toString())
                },
                errorText = errorMedium,
                isFocused = isFocusedMedium,
                onFocusChanged = { isFocusedMedium = it },
                helperText = "This is a Number Field from the Android Design System",
            )

            NumberField(stateMedium)
        }
    }
}