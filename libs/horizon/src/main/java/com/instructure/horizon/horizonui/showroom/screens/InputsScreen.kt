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
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Single Selects SMALL", style = HorizonTypography.p2)
            val state = SingleSelectState(
                size = SingleSelectInputSize.Small,
                options = listOf("Option 1", "Option 2", "Option 3"),
                selectedOption = null,
                onOptionSelected = {},
                onMenuOpenChanged = {},
                helperText = "This is a Single Select from the Android Design System",
            )

            SingleSelect(state.copy(label = "Single Select PLACEHOLDER", placeHolderText = "Select a date"))

            SingleSelect(state.copy(label = "Single Select SELECTED", selectedOption = "Option 1"))

            SingleSelect(state.copy(label = "Single Select FOCUSED", isFocused = true))

            var isOpened1 by remember { mutableStateOf(false) }
            SingleSelect(state.copy(label = "Single Select OPENED FOCUSED", isMenuOpen = isOpened1, onMenuOpenChanged = {isOpened1 = it}, selectedOption = "Option 1"))

            SingleSelect(state.copy(label = "Single Select ERROR", errorText = "This is an error message"))

            var isOpened2 by remember { mutableStateOf(false) }
            SingleSelect(state.copy(label = "Single Select ERROR OPENED", isMenuOpen = isOpened2, onMenuOpenChanged = {isOpened2 = it}, errorText = "This is an error message", selectedOption = "Option 1"))

            SingleSelect(state.copy(label = "Single Select DISABLED", enabled = false, selectedOption = "Option 1"))
        }

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Single Selects MEDIUM", style = HorizonTypography.p2)
            val state = SingleSelectState(
                size = SingleSelectInputSize.Medium,
                options = listOf("Option 1", "Option 2", "Option 3"),
                selectedOption = null,
                onOptionSelected = {},
                onMenuOpenChanged = {},
                helperText = "This is a Single Select from the Android Design System",
            )

            SingleSelect(state.copy(label = "Single Select PLACEHOLDER", placeHolderText = "Select a date"))

            SingleSelect(state.copy(label = "Single Select SELECTED", selectedOption = "Option 1"))

            SingleSelect(state.copy(label = "Single Select FOCUSED", isFocused = true))

            var isOpened1 by remember { mutableStateOf(false) }
            SingleSelect(state.copy(label = "Single Select OPENED FOCUSED", isMenuOpen = isOpened1, onMenuOpenChanged = {isOpened1 = it}, selectedOption = "Option 1"))

            SingleSelect(state.copy(label = "Single Select ERROR", errorText = "This is an error message"))

            var isOpened2 by remember { mutableStateOf(false) }
            SingleSelect(state.copy(label = "Single Select ERROR OPENED", isMenuOpen = isOpened2, onMenuOpenChanged = {isOpened2 = it}, errorText = "This is an error message", selectedOption = "Option 1"))

            SingleSelect(state.copy(label = "Single Select DISABLED", enabled = false, selectedOption = "Option 1"))
        }
    }
}

@Composable
private fun MultiSelectShowcase() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Multiple Selects SMALL", style = HorizonTypography.p2)
            val state = MultiSelectState(
                size = MultiSelectInputSize.Small,
                options = listOf("Option 1", "Option 2", "Option 3"),
                selectedOptions = emptyList(),
                onOptionSelected = {},
                onOptionRemoved = {},
                onMenuOpenChanged = {},
                helperText = "This is a Multiple Selects from the Android Design System",
            )

            MultiSelect(state.copy(label = "Multiple Selects PLACEHOLDER", placeHolderText = "Select a date"))

            MultiSelect(state.copy(label = "Multiple Selects SELECTED", selectedOptions = listOf("Option 1", "Option 2")))

            MultiSelect(state.copy(label = "Multiple Selects FOCUSED", isFocused = true))

            var isOpened1 by remember { mutableStateOf(false) }
            MultiSelect(state.copy(label = "Multiple Selects OPENED FOCUSED", isMenuOpen = isOpened1, onMenuOpenChanged = {isOpened1 = it}, selectedOptions = listOf("Option 1", "Option 2")))

            MultiSelect(state.copy(label = "Multiple Selects ERROR", errorText = "This is an error message"))

            var isOpened2 by remember { mutableStateOf(false) }
            MultiSelect(state.copy(label = "Multiple Selects ERROR OPENED", isMenuOpen = isOpened2, onMenuOpenChanged = {isOpened2 = it}, errorText = "This is an error message", selectedOptions = listOf("Option 1", "Option 2")))

            MultiSelect(state.copy(label = "Multiple Selects DISABLED", enabled = false, selectedOptions = listOf("Option 1", "Option 2")))
        }

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Multiple Selects MEDIUM", style = HorizonTypography.p2)
            val state = MultiSelectState(
                size = MultiSelectInputSize.Medium,
                options = listOf("Option 1", "Option 2", "Option 3"),
                selectedOptions = emptyList(),
                onOptionSelected = {},
                onOptionRemoved = {},
                onMenuOpenChanged = {},
                helperText = "This is a Single Select from the Android Design System",
            )

            MultiSelect(state.copy(label = "Date Picker PLACEHOLDER", placeHolderText = "Select a date"))

            MultiSelect(state.copy(label = "Date Picker SELECTED", selectedOptions = listOf("Option 1", "Option 2")))

            MultiSelect(state.copy(label = "Date Picker FOCUSED", isFocused = true))

            var isOpened1 by remember { mutableStateOf(false) }
            MultiSelect(state.copy(label = "Date Picker OPENED", isMenuOpen = isOpened1, onMenuOpenChanged = { isOpened1 = it }, selectedOptions = listOf("Option 1", "Option 2")))
            MultiSelect(state.copy(label = "Date Picker ERROR", errorText = "This is an error message"))

            var isOpened2 by remember { mutableStateOf(false) }
            MultiSelect(state.copy(label = "Date Picker ERROR OPENED", isMenuOpen = isOpened2, onMenuOpenChanged = { isOpened2 = it }, errorText = "This is an error message", selectedOptions = listOf("Option 1", "Option 2")))

            MultiSelect(state.copy(label = "Date Picker DISABLED", enabled = false, selectedOptions = listOf("Option 1", "Option 2")))
        }
    }
}

@Composable
private fun DatePickerShowcase() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Date Pickers SMALL", style = HorizonTypography.p2)
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

            DatePicker(state.copy(label = "Date Picker SELECTED", selectedDate = Date()))

            DatePicker(state.copy(label = "Date Picker FOCUSED", isFocused = true))

            DatePicker(state.copy(label = "Date Picker ERROR", errorText = "This is an error message"))

            DatePicker(state.copy(label = "Date Picker DISABLED", enabled = false, selectedDate = Date()))
        }

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Date Pickers MEDIUM", style = HorizonTypography.p2)
            val state = DatePickerState(
                size = DatePickerInputSize.Medium,
                helperText = "This is a Date Picker from the Android Design System",
            )

            DatePicker(state.copy(label = "Date Picker PLACEHOLDER", placeHolderText = "Select a date"))

            DatePicker(state.copy(label = "Date Picker SELECTED", selectedDate = Date()))

            DatePicker(state.copy(label = "Date Picker FOCUSED", isFocused = true))

            DatePicker(state.copy(label = "Date Picker ERROR", errorText = "This is an error message"))

            DatePicker(state.copy(label = "Date Picker DISABLED", enabled = false, selectedDate = Date()))
        }
    }
}

@Composable
private fun TimePickerShowcase() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Time Pickers SMALL", style = HorizonTypography.p2)
            val state = TimePickerState(
                size = TimePickerInputSize.Small,
                helperText = "This is a Time Picker from the Android Design System",
            )

            TimePicker(state.copy(label = "Time PickerPLACEHOLDER", placeHolderText = "Select a time"))

            TimePicker(state.copy(label = "Time Picker SELECTED", selectedTime = LocalTime.now()))

            TimePicker(state.copy(label = "Time Picker FOCUSED", isFocused = true))

            TimePicker(state.copy(label = "Time Picker ERROR", errorText = "This is an error message"))

            TimePicker(state.copy(label = "Time Picker DISABLED", enabled = false, selectedTime = LocalTime.now()))
        }

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Time Pickers MEDIUM", style = HorizonTypography.p2)
            val state = TimePickerState(
                size = TimePickerInputSize.Medium,
                helperText = "This is a Time Picker from the Android Design System",
            )

            TimePicker(state.copy(label = "Time Picker PLACEHOLDER", placeHolderText = "Select a time"))

            TimePicker(state.copy(label = "Time Picker SELECTED", selectedTime = LocalTime.now()))

            TimePicker(state.copy(label = "Time Picker FOCUSED", isFocused = true))

            TimePicker(state.copy(label = "Time Picker ERROR", errorText = "This is an error message"))

            TimePicker(state.copy(label = "Time Picker DISABLED", enabled = false, selectedTime = LocalTime.now()))
        }
    }
}

@Composable
private fun TextAreaShowcase() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Text Area", style = HorizonTypography.p2)
        val modifier = Modifier
            .defaultMinSize(minHeight = 100.dp)
        val state = TextAreaState(
            value = TextFieldValue(""),
            onValueChange = {},
            helperText = "This is a Time Picker from the Android Design System",
        )

        TextArea(state.copy(label = "Text Area PLACEHOLDER", placeHolderText = "Type a text"), modifier)

        TextArea(state.copy(label = "Text Area SELECTED", value = TextFieldValue("This \n is \n a \n very \n long \n text")), modifier)

        TextArea(state.copy(label = "Text Area FOCUSED", isFocused = true), modifier)

        TextArea(state.copy(label = "Text Area ERROR", errorText = "This is an error message"), modifier)

        TextArea(state.copy(label = "Text Area DISABLED", enabled = false, value = TextFieldValue("This \n is \n a \n very \n long \n text")), modifier)
    }
}

@Composable
private fun TextFieldShowcase() {
    Column (verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Text Field SMALL", style = HorizonTypography.p2)
            val state = TextFieldState(
                value = TextFieldValue(""),
                size = TextFieldInputSize.Small,
                onValueChange = {},
                helperText = "This is a Text Field from the Android Design System",
            )

            TextField(state.copy(label = "Text Field PLACEHOLDER", placeHolderText = "Type a text"))

            TextField(state.copy(label = "Text Field SELECTED", value = TextFieldValue("This a text")))

            TextField(state.copy(label = "Text Field FOCUSED", isFocused = true))

            TextField(state.copy(label = "Text Field ERROR", errorText = "This is an error message"))

            TextField(state.copy(label = "Text Field DISABLED", enabled = false, value = TextFieldValue("This a text")))
        }

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Text Field MEDIUM", style = HorizonTypography.p2)
            val state = TextFieldState(
                value = TextFieldValue(""),
                size = TextFieldInputSize.Medium,
                onValueChange = {},
                helperText = "This is a Text Field from the Android Design System",
            )

            TextField(state.copy(label = "Text Field PLACEHOLDER", placeHolderText = "Type a text"))

            TextField(state.copy(label = "Text Field SELECTED", value = TextFieldValue("This a text")))

            TextField(state.copy(label = "Text Field FOCUSED", isFocused = true))

            TextField(state.copy(label = "Text Field ERROR", errorText = "This is an error message"))

            TextField(state.copy(label = "Text Field DISABLED", enabled = false, value = TextFieldValue("This a text")))
        }
    }
}

@Composable
private fun NumberFieldShowcase() {
    Column (verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Number Field SMALL", style = HorizonTypography.p2)
            val state = NumberFieldState(
                value = TextFieldValue(""),
                size = NumberFieldInputSize.Small,
                onValueChange = {},
                helperText = "This is a Number Field from the Android Design System",
            )

            NumberField(state.copy(label = "Number Field PLACEHOLDER", placeHolderText = "Type a number"))

            NumberField(state.copy(label = "Number Field PLACEHOLDER with BUTTONS", placeHolderText = "Type a number", showIncreaseDecreaseButtons = true))

            NumberField(state.copy(label = "Number Field SELECTED", value = TextFieldValue("1")))

            NumberField(state.copy(label = "Number Field FOCUSED", isFocused = true))

            NumberField(state.copy(label = "Number Field ERROR", errorText = "This is an error message"))

            NumberField(state.copy(label = "Number Field DISABLED", enabled = false, value = TextFieldValue("1")))
        }

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Number Field MEDIUM", style = HorizonTypography.p2)
            val state = NumberFieldState(
                value = TextFieldValue(""),
                size = NumberFieldInputSize.Medium,
                onValueChange = {},
                helperText = "This is a Text Field from the Android Design System",
            )

            NumberField(state.copy(label = "Number Field PLACEHOLDER", placeHolderText = "Type a number"))

            NumberField(state.copy(label = "Number Field PLACEHOLDER with BUTTONS", placeHolderText = "Type a number", showIncreaseDecreaseButtons = true))

            NumberField(state.copy(label = "Number Field SELECTED", value = TextFieldValue("1")))

            NumberField(state.copy(label = "Number Field FOCUSED", isFocused = true))

            NumberField(state.copy(label = "Number Field ERROR", errorText = "This is an error message"))

            NumberField(state.copy(label = "Number Field DISABLED", enabled = false, value = TextFieldValue("1")))
        }
    }
}