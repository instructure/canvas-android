// DO NOT EDIT. This is code generated via package:intl/generate_localized.dart
// This is a library that provides messages for a es locale. All the
// messages from the main program should be duplicated here with the same
// function name.

// Ignore issues from commonly used lints in this file.
// ignore_for_file:unnecessary_brace_in_string_interps, unnecessary_new
// ignore_for_file:prefer_single_quotes,comment_references, directives_ordering
// ignore_for_file:annotate_overrides,prefer_generic_function_type_aliases
// ignore_for_file:unused_import, file_names

import 'package:intl/intl.dart';
import 'package:intl/message_lookup_by_library.dart';

final messages = new MessageLookup();

typedef String MessageIfAbsent(String messageStr, List<dynamic> args);

class MessageLookup extends MessageLookupByLibrary {
  String get localeName => 'es';

  static m0(userName) => "Está actuando en nombre de ${userName}";

  static m1(version) => "v. ${version}";

  static m2(threshold) => "Calificación de la tarea superior a ${threshold}";

  static m3(threshold) => "Calificación de la tarea inferior a ${threshold}";

  static m4(moduleName) => "Esta tarea está bloqueada por el módulo \"${moduleName}\".";

  static m5(studentName, assignmentName) => "Asunto: ${studentName}, Tarea: ${assignmentName}";

  static m6(points) => "${points} ptos.";

  static m7(points) => "${points} puntos";

  static m8(authorName, howMany) => "${Intl.plural(howMany, one: '${authorName} a 1 otro', other: '${authorName} a ${howMany} otros')}";

  static m9(authorName, recipientName) => "${authorName} a ${recipientName}";

  static m10(authorName, recipientName, howMany) => "${Intl.plural(howMany, one: '${authorName} a ${recipientName} y 1 otro', other: '${authorName} a ${recipientName} y ${howMany} otros')}";

  static m11(count) => "${count}+";

  static m12(studentName) => "Cambiar color para ${studentName}";

  static m13(score, pointsPossible) => "${score} de ${pointsPossible} puntos";

  static m14(studentShortName) => "para ${studentShortName}";

  static m15(threshold) => "Calificación del curso superior a ${threshold}";

  static m16(threshold) => "Calificación del curso inferior a ${threshold}";

  static m17(date, time) => "${date} a las ${time}";

  static m18(alertTitle) => "Descartar ${alertTitle}";

  static m19(canvasGuides, canvasSupport) => "Intente buscar el nombre de la escuela o el distrito al que intenta acceder, como “Smith Private School” o “Smith County Schools”. También puede ingresar directamente a un dominio de Canvas, como “smith.instructure.com”.\n\nA fin de obtener más información sobre cómo encontrar la cuenta de Canvas de su institución, puede visitar ${canvasGuides}, comunicarse con ${canvasSupport} o ponerse en contacto con su escuela para recibir asistencia.";

  static m20(date, time) => "Fecha límite el ${date} a las ${time}";

  static m21(userName) => "Dejará de actuar en nombre de ${userName} y cerrará la sesión.";

  static m22(userName) => "Dejará de actuar en nombre de ${userName} y regresará a su cuenta original.";

  static m23(studentName, eventTitle) => "Asunto: ${studentName}, Evento: ${eventTitle}";

  static m24(startAt, endAt) => "${startAt} - ${endAt}";

  static m25(grade) => "Calificación final: ${grade}";

  static m26(studentName) => "Asunto: ${studentName}, Página de inicio";

  static m27(score, pointsPossible) => "${score} / ${pointsPossible}";

  static m28(studentName) => "Asunto: ${studentName}, Calificaciones";

  static m29(pointsLost) => "Sanción por presentación con atraso (-${pointsLost})";

  static m30(studentName, linkUrl) => "Asunto: ${studentName}, ${linkUrl}";

  static m31(percentage) => "Debe ser superior a ${percentage}";

  static m32(percentage) => "Debe ser inferior a ${percentage}";

  static m33(month) => "Próximo mes: ${month}";

  static m34(date) => "La próxima semana comienza el ${date}";

  static m35(query) => "No se pudieron encontrar escuelas que coincidan con \"${query}\".";

  static m36(points, howMany) => "${Intl.plural(howMany, one: 'De 1 punto', other: 'De ${points} puntos')}";

  static m37(count) => "+${count}";

  static m38(points) => "${points} puntos posibles";

  static m39(month) => "Mes anterior: ${month}";

  static m40(date) => "La semana anterior comienza el ${date}";

  static m41(termsOfService, privacyPolicy) => "Al pulsar en ‘Crear cuenta’ (Create Account), acepta los ${termsOfService} y la ${privacyPolicy}";

  static m42(version) => "Sugerencias para Android: Canvas Parent ${version}";

  static m43(month) => "Mes de ${month}";

  static m44(position) => "${Intl.plural(position, one: '${position} estrella', other: '${position} estrellas')}";

  static m45(date, time) => "Esta tarea se envió el ${date} a las ${time} y está a la espera de su calificación";

  static m46(studentName) => "Asunto: ${studentName}, Programa del curso";

  static m47(count) => "${count} no leído";

  final messages = _notInlinedMessages(_notInlinedMessages);
  static _notInlinedMessages(_) => <String, Function> {
    "\"Act as\" is essentially logging in as this user without a password. You will be able to take any action as if you were this user, and from other users\' points of views, it will be as if this user performed them. However, audit logs record that you were the one who performed the actions on behalf of this user." : MessageLookupByLibrary.simpleMessage("\"Actuar en nombre de\" es esencialmente iniciar sesión como este usuario sin una contraseña. Podrá realizar cualquier acción como si fuera este usuario y, desde el punto de vista de los demás usuarios, será como si este usuario las hubiera realizado. Sin embargo, los registros de auditoría indican que usted es quien realizó las acciones en representación de este usuario."),
    "-" : MessageLookupByLibrary.simpleMessage("-"),
    "A description is required." : MessageLookupByLibrary.simpleMessage("La descripción es obligatoria."),
    "A network error occurred when adding this student. Check your connection and try again." : MessageLookupByLibrary.simpleMessage("Hubo un error de la red al agregar a este estudiante. Compruebe su conexión y vuelva a intentarlo."),
    "A subject is required." : MessageLookupByLibrary.simpleMessage("El asunto es obligatorio."),
    "Act As User" : MessageLookupByLibrary.simpleMessage("Actuar en nombre de Usuario"),
    "Add Student" : MessageLookupByLibrary.simpleMessage("Agregar estudiante"),
    "Add attachment" : MessageLookupByLibrary.simpleMessage("Agregar adjunto"),
    "Add new student" : MessageLookupByLibrary.simpleMessage("Agregar nuevo estudiante"),
    "Add student with…" : MessageLookupByLibrary.simpleMessage("Agregar estudiante con..."),
    "Alert Settings" : MessageLookupByLibrary.simpleMessage("Configuraciones de alertas"),
    "Alert me when…" : MessageLookupByLibrary.simpleMessage("Enviarme una alerta cuando..."),
    "All Grading Periods" : MessageLookupByLibrary.simpleMessage("Todos los períodos de calificación"),
    "Already have an account? " : MessageLookupByLibrary.simpleMessage("¿Ya tiene una cuenta? "),
    "An email address is required." : MessageLookupByLibrary.simpleMessage("La dirección de correo electrónico es obligatoria."),
    "An error occurred when trying to display this link" : MessageLookupByLibrary.simpleMessage("Se produjo un error al intentar mostrar este enlace"),
    "An error occurred while saving your selection. Please try again." : MessageLookupByLibrary.simpleMessage("Ocurrió un error al guardar su selección. Inténtelo de nuevo."),
    "An unexpected error occurred" : MessageLookupByLibrary.simpleMessage("Ha ocurrido un error inesperado"),
    "Android OS version" : MessageLookupByLibrary.simpleMessage("Versión del SO de Android"),
    "Appearance" : MessageLookupByLibrary.simpleMessage("Apariencia"),
    "Application version" : MessageLookupByLibrary.simpleMessage("Versión de la aplicación"),
    "Are you a student or teacher?" : MessageLookupByLibrary.simpleMessage("¿Es estudiante o profesor?"),
    "Are you sure you want to log out?" : MessageLookupByLibrary.simpleMessage("¿Está seguro de que desea salir?"),
    "Are you sure you wish to close this page? Your unsent message will be lost." : MessageLookupByLibrary.simpleMessage("¿Está seguro de que desea cerrar esta página? Su mensaje no enviado se perderá."),
    "Assignment Details" : MessageLookupByLibrary.simpleMessage("Detalles de la tarea"),
    "Assignment grade above" : MessageLookupByLibrary.simpleMessage("Calificación de la tarea superior a"),
    "Assignment grade below" : MessageLookupByLibrary.simpleMessage("Calificación de la tarea inferior a"),
    "Assignment missing" : MessageLookupByLibrary.simpleMessage("Tarea faltante"),
    "Barney, Fuschia" : MessageLookupByLibrary.simpleMessage("Fucsia tipo Barney"),
    "Calendars" : MessageLookupByLibrary.simpleMessage("Calendarios"),
    "Camera Permission" : MessageLookupByLibrary.simpleMessage("Permiso de la cámara"),
    "Cancel" : MessageLookupByLibrary.simpleMessage("Cancelar"),
    "Canvas Student" : MessageLookupByLibrary.simpleMessage("Estudiante de Canvas"),
    "Canvas Teacher" : MessageLookupByLibrary.simpleMessage("Canvas Teacher"),
    "Canvas on GitHub" : MessageLookupByLibrary.simpleMessage("Canvas en GitHub"),
    "Choose a course to message" : MessageLookupByLibrary.simpleMessage("Elija un curso para enviar un mensaje"),
    "Choose from Gallery" : MessageLookupByLibrary.simpleMessage("Elegir de la galería"),
    "Complete" : MessageLookupByLibrary.simpleMessage("Completa"),
    "Contact Support" : MessageLookupByLibrary.simpleMessage("Póngase en contacto con el Soporte técnico"),
    "Course Announcement" : MessageLookupByLibrary.simpleMessage("Anuncio del curso"),
    "Course Announcements" : MessageLookupByLibrary.simpleMessage("Anuncios del curso"),
    "Course grade above" : MessageLookupByLibrary.simpleMessage("Calificación del curso superior a"),
    "Course grade below" : MessageLookupByLibrary.simpleMessage("Calificación del curso inferior a"),
    "Create Account" : MessageLookupByLibrary.simpleMessage("Crear cuenta"),
    "Dark Mode" : MessageLookupByLibrary.simpleMessage("Modo oscuro"),
    "Date" : MessageLookupByLibrary.simpleMessage("Fecha"),
    "Delete" : MessageLookupByLibrary.simpleMessage("Eliminar"),
    "Description" : MessageLookupByLibrary.simpleMessage("Descripción"),
    "Device" : MessageLookupByLibrary.simpleMessage("Dispositivo"),
    "Device model" : MessageLookupByLibrary.simpleMessage("Modelo del dispositivo"),
    "Domain" : MessageLookupByLibrary.simpleMessage("Dominio"),
    "Domain:" : MessageLookupByLibrary.simpleMessage("Dominio:"),
    "Don\'t show again" : MessageLookupByLibrary.simpleMessage("No mostrar nuevamente"),
    "Done" : MessageLookupByLibrary.simpleMessage("Listo"),
    "Download" : MessageLookupByLibrary.simpleMessage("Descargar"),
    "Due" : MessageLookupByLibrary.simpleMessage("Fecha de entrega"),
    "EXTREME CRITICAL EMERGENCY!!" : MessageLookupByLibrary.simpleMessage("EMERGENCIA CRÍTICA"),
    "Electric, blue" : MessageLookupByLibrary.simpleMessage("Azul eléctrico"),
    "Email Address" : MessageLookupByLibrary.simpleMessage("Dirección de correo electrónico"),
    "Email:" : MessageLookupByLibrary.simpleMessage("Correo electrónico:"),
    "Email…" : MessageLookupByLibrary.simpleMessage("Correo electrónico…"),
    "Enter the student pairing code provided to you. If the pairing code doesn\'t work, it may have expired" : MessageLookupByLibrary.simpleMessage("Ingrese el código de emparejamiento de estudiantes que se le proporcionó. Si el código de emparejamiento no funciona, es posible que haya caducado"),
    "Event" : MessageLookupByLibrary.simpleMessage("Evento"),
    "Excused" : MessageLookupByLibrary.simpleMessage("Justificado"),
    "Expired QR Code" : MessageLookupByLibrary.simpleMessage("Código QR vencido"),
    "Failed. Tap for options." : MessageLookupByLibrary.simpleMessage("Se produjo un error. Pulse para ver las opciones."),
    "Filter" : MessageLookupByLibrary.simpleMessage("Filtrar"),
    "Filter by" : MessageLookupByLibrary.simpleMessage("Filtrar por"),
    "Fire, Orange" : MessageLookupByLibrary.simpleMessage("Naranja tipo fuego"),
    "Front Page" : MessageLookupByLibrary.simpleMessage("Página de inicio"),
    "Full Name" : MessageLookupByLibrary.simpleMessage("Nombre completo"),
    "Full Name…" : MessageLookupByLibrary.simpleMessage("Nombre completo…"),
    "Full error message" : MessageLookupByLibrary.simpleMessage("Mensaje de error completo"),
    "Go to today" : MessageLookupByLibrary.simpleMessage("Ir a la fecha de hoy"),
    "Grade" : MessageLookupByLibrary.simpleMessage("Calificación"),
    "Grade percentage" : MessageLookupByLibrary.simpleMessage("Porcentaje de calificación"),
    "Graded" : MessageLookupByLibrary.simpleMessage("Calificado"),
    "Grades" : MessageLookupByLibrary.simpleMessage("Calificaciones"),
    "Help" : MessageLookupByLibrary.simpleMessage("Ayuda"),
    "Hide Password" : MessageLookupByLibrary.simpleMessage("Ocultar contraseña"),
    "High Contrast Mode" : MessageLookupByLibrary.simpleMessage("Modo de alto contraste"),
    "How are we doing?" : MessageLookupByLibrary.simpleMessage("¿Cómo está?"),
    "How is this affecting you?" : MessageLookupByLibrary.simpleMessage("¿Cómo le afecta esto?"),
    "I can\'t get things done until I hear back from you." : MessageLookupByLibrary.simpleMessage("No puedo terminar mis asuntos hasta que reciba una respuesta suya."),
    "I don\'t have a Canvas account" : MessageLookupByLibrary.simpleMessage("No tengo cuenta de Canvas"),
    "I have a Canvas account" : MessageLookupByLibrary.simpleMessage("Tengo una cuenta de Canvas"),
    "I need some help but it\'s not urgent." : MessageLookupByLibrary.simpleMessage("Necesito un poco de ayuda, pero no es urgente."),
    "I\'m having trouble logging in" : MessageLookupByLibrary.simpleMessage("Tengo problemas para iniciar sesión"),
    "Idea for Canvas Parent App [Android]" : MessageLookupByLibrary.simpleMessage("Ideas para la aplicación de Canvas Parent [Android]"),
    "In order to provide you with a better experience, we have updated how reminders work. You can add new reminders by viewing an assignment or calendar event and tapping the switch under the \"Remind Me\" section.\n\nBe aware that any reminders created with older versions of this app will not be compatible with the new changes and you will need to create them again." : MessageLookupByLibrary.simpleMessage("Para brindarle una mejor experiencia, hemos actualizado el funcionamiento de los recordatorios. Para agregar nuevos recordatorios, vea una tarea o evento del calendario y pulse el botón en la sección \"Recordarme\" (\"Remind Me\").\n\nTenga presente que cualquier recordatorio creado con versiones anteriores de esta aplicación no será compatible con los nuevos cambios, y deberá volver a crearlo."),
    "Inbox" : MessageLookupByLibrary.simpleMessage("Buzón de entrada"),
    "Inbox Zero" : MessageLookupByLibrary.simpleMessage("Bandeja de entrada vacía"),
    "Incomplete" : MessageLookupByLibrary.simpleMessage("Incompleta"),
    "Incorrect Domain" : MessageLookupByLibrary.simpleMessage("Dominio incorrecto"),
    "Institution Announcement" : MessageLookupByLibrary.simpleMessage("Anuncio de la institución"),
    "Institution Announcements" : MessageLookupByLibrary.simpleMessage("Anuncios de la institución"),
    "Instructions" : MessageLookupByLibrary.simpleMessage("Instrucciones"),
    "Interactions on this page are limited by your institution." : MessageLookupByLibrary.simpleMessage("Las interacciones en esta página están limitadas por su institución."),
    "Invalid QR Code" : MessageLookupByLibrary.simpleMessage("Código QR inválido"),
    "It looks like a great day to rest, relax, and recharge." : MessageLookupByLibrary.simpleMessage("Este parece ser un día excelente para descansar, relajarse y recargar energías."),
    "It looks like assignments haven\'t been created in this space yet." : MessageLookupByLibrary.simpleMessage("Parece que aún no se han creado tareas en este espacio."),
    "Just a casual question, comment, idea, suggestion…" : MessageLookupByLibrary.simpleMessage("Sólo una pregunta, comentario, idea, sugerencia casual..."),
    "Late" : MessageLookupByLibrary.simpleMessage("Atrasado"),
    "Launch External Tool" : MessageLookupByLibrary.simpleMessage("Iniciar la herramienta externa"),
    "Legal" : MessageLookupByLibrary.simpleMessage("Legal"),
    "Light Mode" : MessageLookupByLibrary.simpleMessage("Modo claro"),
    "Link Error" : MessageLookupByLibrary.simpleMessage("Error de enlace"),
    "Locale:" : MessageLookupByLibrary.simpleMessage("Lugar:"),
    "Locate QR Code" : MessageLookupByLibrary.simpleMessage("Ubicar código QR"),
    "Location" : MessageLookupByLibrary.simpleMessage("Ubicación"),
    "Locked" : MessageLookupByLibrary.simpleMessage("Bloqueado"),
    "Log Out" : MessageLookupByLibrary.simpleMessage("Cerrar sesión"),
    "Login flow: Canvas" : MessageLookupByLibrary.simpleMessage("Flujo de inicio de sesión: Canvas"),
    "Login flow: Normal" : MessageLookupByLibrary.simpleMessage("Flujo de inicio de sesión: Normal"),
    "Login flow: Site Admin" : MessageLookupByLibrary.simpleMessage("Flujo de inicio de sesión: Administrador del sitio"),
    "Login flow: Skip mobile verify" : MessageLookupByLibrary.simpleMessage("Flujo de inicio de sesión: Saltear verificación móvil"),
    "Manage Students" : MessageLookupByLibrary.simpleMessage("Administrar estudiantes"),
    "Message" : MessageLookupByLibrary.simpleMessage("Mensaje"),
    "Message subject" : MessageLookupByLibrary.simpleMessage("Asunto del mensaje"),
    "Missing" : MessageLookupByLibrary.simpleMessage("Faltante"),
    "Must be below 100" : MessageLookupByLibrary.simpleMessage("Debe ser inferior a 100"),
    "Network error" : MessageLookupByLibrary.simpleMessage("Error de red"),
    "Never" : MessageLookupByLibrary.simpleMessage("Nunca"),
    "New message" : MessageLookupByLibrary.simpleMessage("Nuevo mensaje"),
    "No" : MessageLookupByLibrary.simpleMessage("No"),
    "No Alerts" : MessageLookupByLibrary.simpleMessage("Sin alertas"),
    "No Assignments" : MessageLookupByLibrary.simpleMessage("No hay tareas"),
    "No Courses" : MessageLookupByLibrary.simpleMessage("Sin cursos"),
    "No Due Date" : MessageLookupByLibrary.simpleMessage("No hay fecha de entrega"),
    "No Events Today!" : MessageLookupByLibrary.simpleMessage("¡No hay ningún evento hoy!"),
    "No Grade" : MessageLookupByLibrary.simpleMessage("Sin calificación"),
    "No Location Specified" : MessageLookupByLibrary.simpleMessage("Sin ubicación especificada"),
    "No Students" : MessageLookupByLibrary.simpleMessage("No hay estudiantes"),
    "No Subject" : MessageLookupByLibrary.simpleMessage("Sin asunto"),
    "No Summary" : MessageLookupByLibrary.simpleMessage("Sin resumen"),
    "No description" : MessageLookupByLibrary.simpleMessage("Sin descripción"),
    "No recipients selected" : MessageLookupByLibrary.simpleMessage("No seleccionó ningún destinatario"),
    "Not Graded" : MessageLookupByLibrary.simpleMessage("Sin calificar"),
    "Not Submitted" : MessageLookupByLibrary.simpleMessage("No entregado"),
    "Not a parent?" : MessageLookupByLibrary.simpleMessage("¿No es padre?"),
    "Notifications for reminders about assignments and calendar events" : MessageLookupByLibrary.simpleMessage("Notificaciones de recordatorios de tareas y eventos en el calendario"),
    "OS Version" : MessageLookupByLibrary.simpleMessage("Versión del SO"),
    "Observer" : MessageLookupByLibrary.simpleMessage("Observador"),
    "One of our other apps might be a better fit. Tap one to visit the Play Store." : MessageLookupByLibrary.simpleMessage("Una de nuestras otras aplicaciones podría ser más adecuada. Toque una para visitar la Play Store."),
    "Open Canvas Student" : MessageLookupByLibrary.simpleMessage("Abrir Canvas Student"),
    "Open In Browser" : MessageLookupByLibrary.simpleMessage("Abrir en el navegador"),
    "Open with another app" : MessageLookupByLibrary.simpleMessage("Abrir con otra aplicación"),
    "Pairing Code" : MessageLookupByLibrary.simpleMessage("Código de emparejamiento"),
    "Password" : MessageLookupByLibrary.simpleMessage("Contraseña"),
    "Password is required" : MessageLookupByLibrary.simpleMessage("La contraseña es obligatoria"),
    "Password must contain at least 8 characters" : MessageLookupByLibrary.simpleMessage("La contraseña debe contener 8 caracteres como mínimo"),
    "Password…" : MessageLookupByLibrary.simpleMessage("Contraseña…"),
    "Planner Note" : MessageLookupByLibrary.simpleMessage("Nota de la agenda organizadora"),
    "Please enter a valid email address" : MessageLookupByLibrary.simpleMessage("Ingrese una dirección de correo electrónico válida"),
    "Please enter an email address" : MessageLookupByLibrary.simpleMessage("Ingrese una dirección de correo electrónico"),
    "Please enter full name" : MessageLookupByLibrary.simpleMessage("Ingrese el nombre completo"),
    "Please scan a QR code generated by Canvas" : MessageLookupByLibrary.simpleMessage("Escanee un código QR generado por Canvas"),
    "Plum, Purple" : MessageLookupByLibrary.simpleMessage("Morado tipo ciruela"),
    "Preparing…" : MessageLookupByLibrary.simpleMessage("En preparación..."),
    "Previous Logins" : MessageLookupByLibrary.simpleMessage("Inicios de sesión anteriores"),
    "Privacy Policy" : MessageLookupByLibrary.simpleMessage("Política de privacidad"),
    "Privacy Policy Link" : MessageLookupByLibrary.simpleMessage("Enlace a la política de privacidad"),
    "Privacy policy, terms of use, open source" : MessageLookupByLibrary.simpleMessage("Política de privacidad, términos de uso, código abierto"),
    "QR Code" : MessageLookupByLibrary.simpleMessage("Código QR"),
    "QR scanning requires camera access" : MessageLookupByLibrary.simpleMessage("Se requiere acceso a la cámara para escanear el código QR"),
    "Raspberry, Red" : MessageLookupByLibrary.simpleMessage("Rojo tipo mora"),
    "Recipients" : MessageLookupByLibrary.simpleMessage("Destinatarios"),
    "Refresh" : MessageLookupByLibrary.simpleMessage("Actualizar"),
    "Remind Me" : MessageLookupByLibrary.simpleMessage("Recordármelo"),
    "Reminders" : MessageLookupByLibrary.simpleMessage("Recordatorios"),
    "Reminders have changed!" : MessageLookupByLibrary.simpleMessage("¡Se cambiaron los recordatorios!"),
    "Reply" : MessageLookupByLibrary.simpleMessage("Responder"),
    "Reply All" : MessageLookupByLibrary.simpleMessage("Responder a todos"),
    "Report A Problem" : MessageLookupByLibrary.simpleMessage("Reportar un problema"),
    "Request Login Help" : MessageLookupByLibrary.simpleMessage("Solicitar ayuda para iniciar sesión"),
    "Request Login Help Button" : MessageLookupByLibrary.simpleMessage("Botón Solicitar ayuda para iniciar sesión"),
    "Restart app" : MessageLookupByLibrary.simpleMessage("Reiniciar aplicación"),
    "Retry" : MessageLookupByLibrary.simpleMessage("Reintentar"),
    "Return to Login" : MessageLookupByLibrary.simpleMessage("Volver al inicio de sesión"),
    "STUDENT" : MessageLookupByLibrary.simpleMessage("ESTUDIANTE"),
    "Screenshot showing location of QR code generation in browser" : MessageLookupByLibrary.simpleMessage("Captura de pantalla en la que se muestra la ubicación de la generación del código QR en el navegador"),
    "Screenshot showing location of pairing QR code generation in the Canvas Student app" : MessageLookupByLibrary.simpleMessage("Captura de pantalla en la que se muestra la ubicación de la generación del código QR de emparejamiento en la aplicación Canvas Student"),
    "Select" : MessageLookupByLibrary.simpleMessage("Seleccionar"),
    "Select Student Color" : MessageLookupByLibrary.simpleMessage("Seleccionar color del estudiante"),
    "Select recipients" : MessageLookupByLibrary.simpleMessage("Seleccionar destinatarios"),
    "Send Feedback" : MessageLookupByLibrary.simpleMessage("Enviar retroalimentación"),
    "Send a message about this assignment" : MessageLookupByLibrary.simpleMessage("Enviar un mensaje acerca de esta tarea"),
    "Send a message about this course" : MessageLookupByLibrary.simpleMessage("Enviar un mensaje acerca de este curso"),
    "Send message" : MessageLookupByLibrary.simpleMessage("Enviar mensaje"),
    "Set a date and time to be notified of this event." : MessageLookupByLibrary.simpleMessage("Establecer una fecha y un horario para que se me notifique este evento."),
    "Set a date and time to be notified of this specific assignment." : MessageLookupByLibrary.simpleMessage("Establecer una fecha y un horario para que se me notifique esta tarea específica."),
    "Settings" : MessageLookupByLibrary.simpleMessage("Configuraciones"),
    "Shamrock, Green" : MessageLookupByLibrary.simpleMessage("Verde tipo trébol"),
    "Share Your Love for the App" : MessageLookupByLibrary.simpleMessage("Comparta su amor por la aplicación"),
    "Show Password" : MessageLookupByLibrary.simpleMessage("Mostrar contraseña"),
    "Sign In" : MessageLookupByLibrary.simpleMessage("Iniciar sesión"),
    "Something went wrong trying to create your account, please reach out to your school for assistance." : MessageLookupByLibrary.simpleMessage("Algo salió mal al intentar crear su cuenta; comuníquese con su escuela para obtener asistencia."),
    "Something\'s broken but I can work around it to get what I need done." : MessageLookupByLibrary.simpleMessage("Algo no funciona pero puedo trabajar sin ello para terminar lo que necesito."),
    "Stop Acting as User" : MessageLookupByLibrary.simpleMessage("Dejar de actuar en nombre de Usuario"),
    "Student" : MessageLookupByLibrary.simpleMessage("Estudiante"),
    "Student Pairing" : MessageLookupByLibrary.simpleMessage("Emparejamiento del estudiante"),
    "Students can create a QR code using the Canvas Student app on their mobile device" : MessageLookupByLibrary.simpleMessage("Los estudiantes pueden crear un código QR mediante la aplicación Canvas Student en sus dispositivos móviles"),
    "Students can obtain a pairing code through the Canvas website" : MessageLookupByLibrary.simpleMessage("Los estudiantes pueden obtener un código de emparejamiento a través del sitio web de Canvas"),
    "Subject" : MessageLookupByLibrary.simpleMessage("Asunto"),
    "Submitted" : MessageLookupByLibrary.simpleMessage("Entregado"),
    "Successfully submitted!" : MessageLookupByLibrary.simpleMessage("¡Enviada correctamente!"),
    "Summary" : MessageLookupByLibrary.simpleMessage("Resumen"),
    "Switch Users" : MessageLookupByLibrary.simpleMessage("Intercambiar usuarios"),
    "Syllabus" : MessageLookupByLibrary.simpleMessage("Programa del curso"),
    "TA" : MessageLookupByLibrary.simpleMessage("Profesor asistente"),
    "TEACHER" : MessageLookupByLibrary.simpleMessage("PROFESOR"),
    "Tap to favorite the courses you want to see on the Calendar. Select up to 10." : MessageLookupByLibrary.simpleMessage("Pulse para marcar los cursos que desea ver en el Calendario como favoritos. Seleccione hasta 10."),
    "Tap to pair with a new student" : MessageLookupByLibrary.simpleMessage("Pulse para emparejar con un nuevo estudiante"),
    "Tap to select this student" : MessageLookupByLibrary.simpleMessage("Pulse para seleccionar este estudiante"),
    "Tap to show student selector" : MessageLookupByLibrary.simpleMessage("Pulse para ver el selector de estudiantes"),
    "Teacher" : MessageLookupByLibrary.simpleMessage("Profesor"),
    "Tell us about your favorite parts of the app" : MessageLookupByLibrary.simpleMessage("Cuéntenos sobre sus partes favoritas de la aplicación"),
    "Terms of Service" : MessageLookupByLibrary.simpleMessage("Términos de servicio"),
    "Terms of Service Link" : MessageLookupByLibrary.simpleMessage("Enlace a los términos de servicio"),
    "Terms of Use" : MessageLookupByLibrary.simpleMessage("Términos de uso"),
    "The QR code you scanned may have expired. Refresh the code on the student\'s device and try again." : MessageLookupByLibrary.simpleMessage("Es posible que el código QR que escaneó haya vencido. Actualice el código en el dispositivo del estudiante y vuelva a intentarlo."),
    "The following information will help us better understand your idea:" : MessageLookupByLibrary.simpleMessage("La siguiente información nos ayudará a comprender mejor su idea:"),
    "The server you entered is not authorized for this app." : MessageLookupByLibrary.simpleMessage("El servidor ingresado no está autorizado para esta aplicación."),
    "The student you are trying to add belongs to a different school. Log in or create an account with that school to scan this code." : MessageLookupByLibrary.simpleMessage("El estudiante que está intentando agregar pertenece a una escuela diferente. Inicie sesión o cree una cuenta con esa escuela para escanear el código."),
    "The user agent for this app is not authorized." : MessageLookupByLibrary.simpleMessage("El agente de usuario de esta aplicación no está autorizado."),
    "Theme" : MessageLookupByLibrary.simpleMessage("Tema"),
    "There are no installed applications that can open this file" : MessageLookupByLibrary.simpleMessage("No hay aplicaciones instaladas que puedan abrir este archivo"),
    "There is no page information available." : MessageLookupByLibrary.simpleMessage("No hay información disponible de la página."),
    "There was a problem loading the Terms of Use" : MessageLookupByLibrary.simpleMessage("Hubo un problema al cargar los Términos de uso"),
    "There was a problem removing this student from your account. Please check your connection and try again." : MessageLookupByLibrary.simpleMessage("Ocurrió un problema al eliminar a este estudiante de su cuenta. Compruebe su conexión y vuelva a intentarlo."),
    "There was an error loading recipients for this course" : MessageLookupByLibrary.simpleMessage("Hubo un error al cargar los destinatarios de este curso"),
    "There was an error loading the summary details for this course." : MessageLookupByLibrary.simpleMessage("Hubo un error al cargar los detalles del resumen de este curso."),
    "There was an error loading this announcement" : MessageLookupByLibrary.simpleMessage("Hubo un error al cargar este anuncio"),
    "There was an error loading this conversation" : MessageLookupByLibrary.simpleMessage("Hubo un error al cargar esta conversación"),
    "There was an error loading this file" : MessageLookupByLibrary.simpleMessage("Hubo un error al cargar este archivo"),
    "There was an error loading your inbox messages." : MessageLookupByLibrary.simpleMessage("Hubo un error al cargar sus mensajes del buzón de entrada."),
    "There was an error loading your student\'s alerts." : MessageLookupByLibrary.simpleMessage("Hubo un error al cargar las alertas de sus estudiantes."),
    "There was an error loading your student\'s calendar" : MessageLookupByLibrary.simpleMessage("Hubo un error al cargar el calendario de su estudiante"),
    "There was an error loading your students." : MessageLookupByLibrary.simpleMessage("Hubo un error al cargar sus estudiantes."),
    "There was an error loading your student’s courses." : MessageLookupByLibrary.simpleMessage("Hubo un error al cargar los cursos de su estudiante."),
    "There was an error logging in. Please generate another QR Code and try again." : MessageLookupByLibrary.simpleMessage("Hubo un error al iniciar sesión. Genere otro código QR y vuelva a intentarlo."),
    "There was an error trying to act as this user. Please check the Domain and User ID and try again." : MessageLookupByLibrary.simpleMessage("Hubo un error al intentar actuar en nombre de este usuario. Revise el dominio y la identificación de usuario y vuelva a intentarlo."),
    "There’s nothing to be notified of yet." : MessageLookupByLibrary.simpleMessage("Aún no hay ninguna notificación."),
    "This app is not authorized for use." : MessageLookupByLibrary.simpleMessage("Esta aplicación no está autorizada para usarse."),
    "This course does not have any assignments or calendar events yet." : MessageLookupByLibrary.simpleMessage("Este curso aún no tiene ninguna tarea ni eventos en el calendario."),
    "This file is unsupported and can’t be viewed through the app" : MessageLookupByLibrary.simpleMessage("Este archivo no está admitido y no se puede ver con la aplicación"),
    "This will unpair and remove all enrollments for this student from your account." : MessageLookupByLibrary.simpleMessage("Deshará el emparejamiento y eliminará todas las inscripciones de este estudiante en su cuenta."),
    "Total Grade" : MessageLookupByLibrary.simpleMessage("Calificación total"),
    "Uh oh!" : MessageLookupByLibrary.simpleMessage("¡Ay, no!"),
    "Unable to fetch courses. Please check your connection and try again." : MessageLookupByLibrary.simpleMessage("No se pueden recuperar los cursos. Compruebe su conexión y vuelva a intentarlo."),
    "Unable to load this image" : MessageLookupByLibrary.simpleMessage("No se puede cargar esta imagen"),
    "Unable to play this media file" : MessageLookupByLibrary.simpleMessage("No se puede reproducir este archivo multimedia"),
    "Unable to send message. Check your connection and try again." : MessageLookupByLibrary.simpleMessage("No se puede enviar el mensaje. Compruebe su conexión y vuelva a intentarlo."),
    "Under Construction" : MessageLookupByLibrary.simpleMessage("En construcción"),
    "Unknown User" : MessageLookupByLibrary.simpleMessage("Usuario desconocido"),
    "Unsaved changes" : MessageLookupByLibrary.simpleMessage("Cambios no guardados"),
    "Unsupported File" : MessageLookupByLibrary.simpleMessage("Archivo no admitido"),
    "Upload File" : MessageLookupByLibrary.simpleMessage("Cargar archivo"),
    "Use Camera" : MessageLookupByLibrary.simpleMessage("Usar cámara"),
    "Use Dark Theme in Web Content" : MessageLookupByLibrary.simpleMessage("Usar tema oscuro en el contenido web"),
    "User ID" : MessageLookupByLibrary.simpleMessage("Identificación de usuario"),
    "User ID:" : MessageLookupByLibrary.simpleMessage("Identificación del usuario:"),
    "Version Number" : MessageLookupByLibrary.simpleMessage("Número de versión"),
    "View Description" : MessageLookupByLibrary.simpleMessage("Ver descripción"),
    "View error details" : MessageLookupByLibrary.simpleMessage("Ver detalles del error"),
    "View the Privacy Policy" : MessageLookupByLibrary.simpleMessage("Ver la Política de privacidad"),
    "We are currently building this feature for your viewing pleasure." : MessageLookupByLibrary.simpleMessage("Actualmente estamos desarrollando esta función para que pueda disfrutarla."),
    "We are unable to display this link, it may belong to an institution you currently aren\'t logged in to." : MessageLookupByLibrary.simpleMessage("No podemos mostrar este enlace; es posible que pertenezca a una institución en la que no tiene sesión abierta actualmente."),
    "We couldn\'t find any students associated with this account" : MessageLookupByLibrary.simpleMessage("No pudimos encontrar ningún estudiante relacionado con esta cuenta"),
    "We were unable to verify the server for use with this app." : MessageLookupByLibrary.simpleMessage("No pudimos verificar el servidor para su uso con esta aplicación."),
    "We’re not sure what happened, but it wasn’t good. Contact us if this keeps happening." : MessageLookupByLibrary.simpleMessage("No sabemos bien qué sucedió, pero no fue bueno. Comuníquese con nosotros si esto sigue sucediendo."),
    "What can we do better?" : MessageLookupByLibrary.simpleMessage("¿Qué podemos mejorar?"),
    "Yes" : MessageLookupByLibrary.simpleMessage("Sí"),
    "You are not observing any students." : MessageLookupByLibrary.simpleMessage("No está observando a ningún estudiante."),
    "You may only choose 10 calendars to display" : MessageLookupByLibrary.simpleMessage("Solo puede elegir 10 calendarios para mostrar"),
    "You must enter a user id" : MessageLookupByLibrary.simpleMessage("Debe ingresar una identificación de usuario"),
    "You must enter a valid domain" : MessageLookupByLibrary.simpleMessage("Debe ingresar un dominio válido"),
    "You must select at least one calendar to display" : MessageLookupByLibrary.simpleMessage("Debe seleccionar al menos un calendario para mostrar"),
    "You will be notified about this assignment on…" : MessageLookupByLibrary.simpleMessage("Se le notificará acerca de esta tarea el..."),
    "You will be notified about this event on…" : MessageLookupByLibrary.simpleMessage("Se le notificará acerca de este evento el..."),
    "You\'ll find the QR code on the web in your account profile. Click \'QR for Mobile Login\' in the list." : MessageLookupByLibrary.simpleMessage("Encontrará el código QR en el sitio web, en su perfil de la cuenta. Hacer clic en \"QR para inicio de sesión móvil\" (\"QR for Mobile Login\") en la lista."),
    "You\'ll need to open your student\'s Canvas Student app to continue. Go into Main Menu > Settings > Pair with Observer and scan the QR code you see there." : MessageLookupByLibrary.simpleMessage("Para continuar, debe abrir la aplicación Canvas Student de su estudiante. Diríjase a Menú Principal > Configuraciones > Emparejar con Observador (Main Menu > Settings > Pair with Observer) y escanee el código QR que aparece allí."),
    "Your code is incorrect or expired." : MessageLookupByLibrary.simpleMessage("Su código es incorrecto o ha caducado."),
    "Your student’s courses might not be published yet." : MessageLookupByLibrary.simpleMessage("Es posible que los cursos de sus estudiantes aún no estén publicados."),
    "You’re all caught up!" : MessageLookupByLibrary.simpleMessage("¡Ya está al día!"),
    "actingAsUser" : m0,
    "alertsLabel" : MessageLookupByLibrary.simpleMessage("Alertas"),
    "appVersion" : m1,
    "assignmentGradeAboveThreshold" : m2,
    "assignmentGradeBelowThreshold" : m3,
    "assignmentLockedModule" : m4,
    "assignmentSubjectMessage" : m5,
    "assignmentTotalPoints" : m6,
    "assignmentTotalPointsAccessible" : m7,
    "authorToNOthers" : m8,
    "authorToRecipient" : m9,
    "authorToRecipientAndNOthers" : m10,
    "badgeNumberPlus" : m11,
    "calendarLabel" : MessageLookupByLibrary.simpleMessage("Calendario"),
    "canvasGuides" : MessageLookupByLibrary.simpleMessage("Guías de Canvas"),
    "canvasLogoLabel" : MessageLookupByLibrary.simpleMessage("Logotipo de Canvas"),
    "canvasSupport" : MessageLookupByLibrary.simpleMessage("Soporte técnico de Canvas"),
    "changeStudentColorLabel" : m12,
    "collapse" : MessageLookupByLibrary.simpleMessage("colapsar"),
    "collapsed" : MessageLookupByLibrary.simpleMessage("colapsado"),
    "contentDescriptionScoreOutOfPointsPossible" : m13,
    "courseForWhom" : m14,
    "courseGradeAboveThreshold" : m15,
    "courseGradeBelowThreshold" : m16,
    "coursesLabel" : MessageLookupByLibrary.simpleMessage("Cursos"),
    "dateAtTime" : m17,
    "dismissAlertLabel" : m18,
    "domainSearchHelpBody" : m19,
    "domainSearchHelpLabel" : MessageLookupByLibrary.simpleMessage("¿Cómo encuentro mi escuela o distrito?"),
    "domainSearchInputHint" : MessageLookupByLibrary.simpleMessage("Ingresar el nombre de la escuela o el distrito..."),
    "dueDateAtTime" : m20,
    "endMasqueradeLogoutMessage" : m21,
    "endMasqueradeMessage" : m22,
    "eventSubjectMessage" : m23,
    "eventTime" : m24,
    "expand" : MessageLookupByLibrary.simpleMessage("expandir"),
    "expanded" : MessageLookupByLibrary.simpleMessage("expandido"),
    "finalGrade" : m25,
    "findSchool" : MessageLookupByLibrary.simpleMessage("Buscar escuela"),
    "frontPageSubjectMessage" : m26,
    "gradeFormatScoreOutOfPointsPossible" : m27,
    "gradesSubjectMessage" : m28,
    "latePenalty" : m29,
    "me" : MessageLookupByLibrary.simpleMessage("yo"),
    "messageLinkPostscript" : m30,
    "minus" : MessageLookupByLibrary.simpleMessage("menos"),
    "mustBeAboveN" : m31,
    "mustBeBelowN" : m32,
    "next" : MessageLookupByLibrary.simpleMessage("Siguiente"),
    "nextMonth" : m33,
    "nextWeek" : m34,
    "noDomainResults" : m35,
    "ok" : MessageLookupByLibrary.simpleMessage("Aceptar"),
    "outOfPoints" : m36,
    "plusRecipientCount" : m37,
    "pointsPossible" : m38,
    "previousMonth" : m39,
    "previousWeek" : m40,
    "qrCreateAccountTos" : m41,
    "ratingDialogEmailSubject" : m42,
    "selectedMonthLabel" : m43,
    "send" : MessageLookupByLibrary.simpleMessage("enviar"),
    "starRating" : m44,
    "submissionStatusSuccessSubtitle" : m45,
    "syllabusSubjectMessage" : m46,
    "unread" : MessageLookupByLibrary.simpleMessage("no leído"),
    "unreadCount" : m47
  };
}
