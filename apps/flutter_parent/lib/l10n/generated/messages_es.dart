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

  static m12(score, pointsPossible) => "${score} de ${pointsPossible} puntos";

  static m13(studentShortName) => "para ${studentShortName}";

  static m14(threshold) => "Calificación del curso superior a ${threshold}";

  static m15(threshold) => "Calificación del curso inferior a ${threshold}";

  static m16(date, time) => "${date} a las ${time}";

  static m17(canvasGuides, canvasSupport) => "Intente buscar el nombre de la escuela o el distrito al que intenta acceder, como “Smith Private School” o “Smith County Schools”. También puede ingresar directamente a un dominio de Canvas, como “smith.instructure.com”.\n\nA fin de obtener más información sobre cómo encontrar la cuenta de Canvas de su institución, puede visitar ${canvasGuides}, comunicarse con ${canvasSupport} o ponerse en contacto con su escuela para recibir asistencia.";

  static m18(date, time) => "Fecha límite el ${date} a las ${time}";

  static m19(userName) => "Dejará de actuar en nombre de ${userName} y cerrará la sesión.";

  static m20(userName) => "Dejará de actuar en nombre de ${userName} y regresará a su cuenta original.";

  static m21(studentName, eventTitle) => "Asunto: ${studentName}, Evento: ${eventTitle}";

  static m22(startAt, endAt) => "${startAt} - ${endAt}";

  static m23(grade) => "Calificación final: ${grade}";

  static m24(studentName) => "Asunto: ${studentName}, Página de inicio";

  static m25(score, pointsPossible) => "${score} / ${pointsPossible}";

  static m26(studentName) => "Asunto: ${studentName}, Calificaciones";

  static m27(pointsLost) => "Sanción por presentación con atraso (-${pointsLost})";

  static m28(studentName, linkUrl) => "Asunto: ${studentName}, ${linkUrl}";

  static m29(percentage) => "Debe ser superior a ${percentage}";

  static m30(percentage) => "Debe ser inferior a ${percentage}";

  static m31(month) => "Próximo mes: ${month}";

  static m32(date) => "La próxima semana comienza el ${date}";

  static m33(query) => "No se pudieron encontrar escuelas que coincidan con \"${query}\".";

  static m34(points, howMany) => "${Intl.plural(howMany, one: 'De 1 punto', other: 'De ${points} puntos')}";

  static m35(count) => "+${count}";

  static m36(points) => "${points} puntos posibles";

  static m37(month) => "Mes anterior: ${month}";

  static m38(date) => "La semana anterior comienza el ${date}";

  static m39(month) => "Mes de ${month}";

  static m40(date, time) => "Esta tarea se envió el ${date} a las ${time} y está a la espera de su calificación";

  static m41(studentName) => "Asunto: ${studentName}, Programa del curso";

  static m42(count) => "${count} no leído";

  final messages = _notInlinedMessages(_notInlinedMessages);
  static _notInlinedMessages(_) => <String, Function> {
    "\"Act as\" is essentially logging in as this user without a password. You will be able to take any action as if you were this user, and from other users\' points of views, it will be as if this user performed them. However, audit logs record that you were the one who performed the actions on behalf of this user." : MessageLookupByLibrary.simpleMessage("\"Actuar en nombre de\" es esencialmente iniciar sesión como este usuario sin una contraseña. Podrá realizar cualquier acción como si fuera este usuario y, desde el punto de vista de los demás usuarios, será como si este usuario las hubiera realizado. Sin embargo, los registros de auditoría indican que usted es quien realizó las acciones en representación de este usuario."),
    "-" : MessageLookupByLibrary.simpleMessage("-"),
    "A description is required." : MessageLookupByLibrary.simpleMessage("La descripción es obligatoria."),
    "A subject is required." : MessageLookupByLibrary.simpleMessage("El asunto es obligatorio."),
    "Act As User" : MessageLookupByLibrary.simpleMessage("Actuar en nombre de Usuario"),
    "Add Student" : MessageLookupByLibrary.simpleMessage("Agregar estudiante"),
    "Add attachment" : MessageLookupByLibrary.simpleMessage("Agregar adjunto"),
    "Add new student" : MessageLookupByLibrary.simpleMessage("Agregar nuevo estudiante"),
    "Add student with…" : MessageLookupByLibrary.simpleMessage("Agregar estudiante con..."),
    "Alert Settings" : MessageLookupByLibrary.simpleMessage("Configuraciones de alertas"),
    "Alert me when…" : MessageLookupByLibrary.simpleMessage("Enviarme una alerta cuando..."),
    "All Grading Periods" : MessageLookupByLibrary.simpleMessage("Todos los períodos de calificación"),
    "An email address is required." : MessageLookupByLibrary.simpleMessage("La dirección de correo electrónico es obligatoria."),
    "An error occurred when trying to display this link" : MessageLookupByLibrary.simpleMessage("Se produjo un error al intentar mostrar este enlace"),
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
    "Calendars" : MessageLookupByLibrary.simpleMessage("Calendarios"),
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
    "Dark Mode" : MessageLookupByLibrary.simpleMessage("Modo oscuro"),
    "Date" : MessageLookupByLibrary.simpleMessage("Fecha"),
    "Delete" : MessageLookupByLibrary.simpleMessage("Eliminar"),
    "Description" : MessageLookupByLibrary.simpleMessage("Descripción"),
    "Device" : MessageLookupByLibrary.simpleMessage("Dispositivo"),
    "Device model" : MessageLookupByLibrary.simpleMessage("Modelo del dispositivo"),
    "Domain" : MessageLookupByLibrary.simpleMessage("Dominio"),
    "Domain:" : MessageLookupByLibrary.simpleMessage("Dominio:"),
    "Done" : MessageLookupByLibrary.simpleMessage("Listo"),
    "Download" : MessageLookupByLibrary.simpleMessage("Descargar"),
    "Due" : MessageLookupByLibrary.simpleMessage("Fecha límite"),
    "EXTREME CRITICAL EMERGENCY!!" : MessageLookupByLibrary.simpleMessage("EMERGENCIA CRÍTICA"),
    "Email Address" : MessageLookupByLibrary.simpleMessage("Dirección de correo electrónico"),
    "Email:" : MessageLookupByLibrary.simpleMessage("Correo electrónico:"),
    "Enter the student pairing code provided to you. If the pairing code doesn\'t work, it may have expired" : MessageLookupByLibrary.simpleMessage("Ingrese el código de emparejamiento de estudiantes que se le proporcionó. Si el código de emparejamiento no funciona, es posible que haya caducado"),
    "Event" : MessageLookupByLibrary.simpleMessage("Evento"),
    "Excused" : MessageLookupByLibrary.simpleMessage("Justificado"),
    "Failed. Tap for options." : MessageLookupByLibrary.simpleMessage("Se produjo un error. Pulse para ver las opciones."),
    "Filter" : MessageLookupByLibrary.simpleMessage("Filtrar"),
    "Filter by" : MessageLookupByLibrary.simpleMessage("Filtrar por"),
    "Front Page" : MessageLookupByLibrary.simpleMessage("Página de inicio"),
    "Full error message" : MessageLookupByLibrary.simpleMessage("Mensaje de error completo"),
    "Grade" : MessageLookupByLibrary.simpleMessage("Calificación"),
    "Grade percentage" : MessageLookupByLibrary.simpleMessage("Porcentaje de calificación"),
    "Graded" : MessageLookupByLibrary.simpleMessage("Calificado"),
    "Grades" : MessageLookupByLibrary.simpleMessage("Calificaciones"),
    "Help" : MessageLookupByLibrary.simpleMessage("Ayuda"),
    "High Contrast Mode" : MessageLookupByLibrary.simpleMessage("Modo de alto contraste"),
    "How is this affecting you?" : MessageLookupByLibrary.simpleMessage("¿Cómo le afecta esto?"),
    "I can\'t get things done until I hear back from you." : MessageLookupByLibrary.simpleMessage("No puedo terminar mis asuntos hasta que reciba una respuesta suya."),
    "I need some help but it\'s not urgent." : MessageLookupByLibrary.simpleMessage("Necesito un poco de ayuda, pero no es urgente."),
    "I\'m having trouble logging in" : MessageLookupByLibrary.simpleMessage("Tengo problemas para iniciar sesión"),
    "Idea for Canvas Parent App [Android]" : MessageLookupByLibrary.simpleMessage("Ideas para la aplicación de Canvas Parent [Android]"),
    "Inbox" : MessageLookupByLibrary.simpleMessage("Buzón de entrada"),
    "Inbox Zero" : MessageLookupByLibrary.simpleMessage("Buzón de entrada vacío"),
    "Incomplete" : MessageLookupByLibrary.simpleMessage("Incompleta"),
    "Institution Announcement" : MessageLookupByLibrary.simpleMessage("Anuncio de la institución"),
    "Institution Announcements" : MessageLookupByLibrary.simpleMessage("Anuncios de la institución"),
    "Instructions" : MessageLookupByLibrary.simpleMessage("Instrucciones"),
    "Interactions on this page are limited by your institution." : MessageLookupByLibrary.simpleMessage("Las interacciones en esta página están limitadas por su institución."),
    "It looks like a great day to rest, relax, and recharge." : MessageLookupByLibrary.simpleMessage("Este parece ser un día excelente para descansar, relajarse y recargar energías."),
    "It looks like assignments haven\'t been created in this space yet." : MessageLookupByLibrary.simpleMessage("Parece que aún no se han creado tareas en este espacio."),
    "Just a casual question, comment, idea, suggestion…" : MessageLookupByLibrary.simpleMessage("Sólo una pregunta, comentario, idea, sugerencia casual..."),
    "Late" : MessageLookupByLibrary.simpleMessage("Atrasado"),
    "Launch External Tool" : MessageLookupByLibrary.simpleMessage("Iniciar la herramienta externa"),
    "Legal" : MessageLookupByLibrary.simpleMessage("Legal"),
    "Light Mode" : MessageLookupByLibrary.simpleMessage("Modo claro"),
    "Link Error" : MessageLookupByLibrary.simpleMessage("Error de enlace"),
    "Locale:" : MessageLookupByLibrary.simpleMessage("Lugar:"),
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
    "Open In Browser" : MessageLookupByLibrary.simpleMessage("Abrir en el navegador"),
    "Open with another app" : MessageLookupByLibrary.simpleMessage("Abrir con otra aplicación"),
    "Pairing Code" : MessageLookupByLibrary.simpleMessage("Código de emparejamiento"),
    "Preparing…" : MessageLookupByLibrary.simpleMessage("En preparación..."),
    "Previous Logins" : MessageLookupByLibrary.simpleMessage("Inicios de sesión anteriores"),
    "Privacy Policy" : MessageLookupByLibrary.simpleMessage("Política de privacidad"),
    "Privacy policy, terms of use, open source" : MessageLookupByLibrary.simpleMessage("Política de privacidad, términos de uso, código abierto"),
    "QR Code" : MessageLookupByLibrary.simpleMessage("Código QR"),
    "Recipients" : MessageLookupByLibrary.simpleMessage("Destinatarios"),
    "Remind Me" : MessageLookupByLibrary.simpleMessage("Recordármelo"),
    "Reminders" : MessageLookupByLibrary.simpleMessage("Recordatorios"),
    "Reply" : MessageLookupByLibrary.simpleMessage("Responder"),
    "Reply All" : MessageLookupByLibrary.simpleMessage("Responder a todos"),
    "Report A Problem" : MessageLookupByLibrary.simpleMessage("Reportar un problema"),
    "Request Login Help" : MessageLookupByLibrary.simpleMessage("Solicitar ayuda para iniciar sesión"),
    "Request Login Help Button" : MessageLookupByLibrary.simpleMessage("Botón Solicitar ayuda para iniciar sesión"),
    "Restart app" : MessageLookupByLibrary.simpleMessage("Reiniciar aplicación"),
    "Retry" : MessageLookupByLibrary.simpleMessage("Reintentar"),
    "Return to Login" : MessageLookupByLibrary.simpleMessage("Volver al inicio de sesión"),
    "STUDENT" : MessageLookupByLibrary.simpleMessage("ESTUDIANTE"),
    "Select recipients" : MessageLookupByLibrary.simpleMessage("Seleccionar destinatarios"),
    "Send a message about this assignment" : MessageLookupByLibrary.simpleMessage("Enviar un mensaje acerca de esta tarea"),
    "Send a message about this course" : MessageLookupByLibrary.simpleMessage("Enviar un mensaje acerca de este curso"),
    "Send message" : MessageLookupByLibrary.simpleMessage("Enviar mensaje"),
    "Set a date and time to be notified of this event." : MessageLookupByLibrary.simpleMessage("Establecer una fecha y un horario para que se me notifique este evento."),
    "Set a date and time to be notified of this specific assignment." : MessageLookupByLibrary.simpleMessage("Establecer una fecha y un horario para que se me notifique esta tarea específica."),
    "Settings" : MessageLookupByLibrary.simpleMessage("Configuraciones"),
    "Share Your Love for the App" : MessageLookupByLibrary.simpleMessage("Comparta su amor por la aplicación"),
    "Something\'s broken but I can work around it to get what I need done." : MessageLookupByLibrary.simpleMessage("Algo no funciona pero puedo trabajar sin ello para terminar lo que necesito."),
    "Stop Acting as User" : MessageLookupByLibrary.simpleMessage("Dejar de actuar en nombre de Usuario"),
    "Student" : MessageLookupByLibrary.simpleMessage("Estudiante"),
    "Subject" : MessageLookupByLibrary.simpleMessage("Asunto"),
    "Submitted" : MessageLookupByLibrary.simpleMessage("Entregado"),
    "Successfully submitted!" : MessageLookupByLibrary.simpleMessage("¡Enviada correctamente!"),
    "Summary" : MessageLookupByLibrary.simpleMessage("Resumen"),
    "Switch Users" : MessageLookupByLibrary.simpleMessage("Intercambiar usuarios"),
    "Syllabus" : MessageLookupByLibrary.simpleMessage("Programa del curso"),
    "TA" : MessageLookupByLibrary.simpleMessage("Profesor asistente"),
    "TEACHER" : MessageLookupByLibrary.simpleMessage("PROFESOR"),
    "Tap to favorite the courses you want to see on the Calendar." : MessageLookupByLibrary.simpleMessage("Pulse para marcar los cursos que desea ver en el Calendario como favoritos."),
    "Tap to pair with a new student" : MessageLookupByLibrary.simpleMessage("Pulse para emparejar con un nuevo estudiante"),
    "Tap to select this student" : MessageLookupByLibrary.simpleMessage("Pulse para seleccionar este estudiante"),
    "Tap to show student selector" : MessageLookupByLibrary.simpleMessage("Pulse para ver el selector de estudiantes"),
    "Teacher" : MessageLookupByLibrary.simpleMessage("Profesor"),
    "Tell us about your favorite parts of the app" : MessageLookupByLibrary.simpleMessage("Cuéntenos sobre sus partes favoritas de la aplicación"),
    "Terms of Use" : MessageLookupByLibrary.simpleMessage("Términos de uso"),
    "The following information will help us better understand your idea:" : MessageLookupByLibrary.simpleMessage("La siguiente información nos ayudará a comprender mejor su idea:"),
    "The server you entered is not authorized for this app." : MessageLookupByLibrary.simpleMessage("El servidor ingresado no está autorizado para esta aplicación."),
    "The user agent for this app is not authorized." : MessageLookupByLibrary.simpleMessage("El agente de usuario de esta aplicación no está autorizado."),
    "Theme" : MessageLookupByLibrary.simpleMessage("Tema"),
    "There are no installed applications that can open this file" : MessageLookupByLibrary.simpleMessage("No hay aplicaciones instaladas que puedan abrir este archivo"),
    "There is no page information available." : MessageLookupByLibrary.simpleMessage("No hay información disponible de la página."),
    "There was a problem loading the Terms of Use" : MessageLookupByLibrary.simpleMessage("Hubo un problema al cargar los Términos de uso"),
    "There was an error loading recipients for this course" : MessageLookupByLibrary.simpleMessage("Hubo un error al cargar los destinatarios de este curso"),
    "There was an error loading the summary details for this course." : MessageLookupByLibrary.simpleMessage("Hubo un error al cargar los detalles del resumen de este curso."),
    "There was an error loading this announcement" : MessageLookupByLibrary.simpleMessage("Hubo un error al cargar este anuncio"),
    "There was an error loading this conversation" : MessageLookupByLibrary.simpleMessage("Hubo un error al cargar esta conversación"),
    "There was an error loading this file" : MessageLookupByLibrary.simpleMessage("Hubo un error al cargar este archivo"),
    "There was an error loading your inbox messages." : MessageLookupByLibrary.simpleMessage("Hubo un error al cargar sus mensajes del buzón de entrada."),
    "There was an error loading your student\'s alerts." : MessageLookupByLibrary.simpleMessage("Hubo un error al cargar las alertas de sus estudiantes."),
    "There was an error loading your student\'s calendar" : MessageLookupByLibrary.simpleMessage("Hubo un error al cargar el calendario de su estudiante"),
    "There was an error loading your students." : MessageLookupByLibrary.simpleMessage("Hubo un error al cargar sus estudiantes."),
    "There was an error loading your your student’s courses." : MessageLookupByLibrary.simpleMessage("Hubo un error al cargar los cursos de sus estudiantes."),
    "There was an error trying to act as this user. Please check the Domain and User ID and try again." : MessageLookupByLibrary.simpleMessage("Hubo un error al intentar actuar en nombre de este usuario. Revise el dominio y la identificación de usuario y vuelva a intentarlo."),
    "There’s nothing to be notified of yet." : MessageLookupByLibrary.simpleMessage("Aún no hay ninguna notificación."),
    "This app is not authorized for use." : MessageLookupByLibrary.simpleMessage("Esta aplicación no está autorizada para usarse."),
    "This course does not have any assignments or calendar events yet." : MessageLookupByLibrary.simpleMessage("Este curso aún no tiene ninguna tarea ni eventos en el calendario."),
    "This file is unsupported and can’t be viewed through the app" : MessageLookupByLibrary.simpleMessage("Este archivo no está admitido y no se puede ver con la aplicación"),
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
    "User ID" : MessageLookupByLibrary.simpleMessage("Identificación de usuario"),
    "User ID:" : MessageLookupByLibrary.simpleMessage("Identificación del usuario:"),
    "Version Number" : MessageLookupByLibrary.simpleMessage("Número de versión"),
    "View error details" : MessageLookupByLibrary.simpleMessage("Ver detalles del error"),
    "We are currently building this feature for your viewing pleasure." : MessageLookupByLibrary.simpleMessage("Actualmente estamos desarrollando esta función para que pueda disfrutarla."),
    "We are unable to display this link, it may belong to an institution you currently aren\'t logged in to." : MessageLookupByLibrary.simpleMessage("No podemos mostrar este enlace; es posible que pertenezca a una institución en la que no tiene sesión abierta actualmente."),
    "We couldn\'t find any students associated with this account" : MessageLookupByLibrary.simpleMessage("No pudimos encontrar ningún estudiante relacionado con esta cuenta"),
    "We were unable to verify the server for use with this app." : MessageLookupByLibrary.simpleMessage("No pudimos verificar el servidor para su uso con esta aplicación."),
    "We’re not sure what happened, but it wasn’t good. Contact us if this keeps happening." : MessageLookupByLibrary.simpleMessage("No sabemos bien qué sucedió, pero no fue bueno. Comuníquese con nosotros si esto sigue sucediendo."),
    "Yes" : MessageLookupByLibrary.simpleMessage("Sí"),
    "You are not observing any students." : MessageLookupByLibrary.simpleMessage("No está observando a ningún estudiante."),
    "You must enter a user id" : MessageLookupByLibrary.simpleMessage("Debe ingresar una identificación de usuario"),
    "You must enter a valid domain" : MessageLookupByLibrary.simpleMessage("Debe ingresar un dominio válido"),
    "You will be notified about this assignment on…" : MessageLookupByLibrary.simpleMessage("Se le notificará acerca de esta tarea el..."),
    "You will be notified about this event on…" : MessageLookupByLibrary.simpleMessage("Se le notificará acerca de este evento el..."),
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
    "collapse" : MessageLookupByLibrary.simpleMessage("colapsar"),
    "collapsed" : MessageLookupByLibrary.simpleMessage("colapsado"),
    "contentDescriptionScoreOutOfPointsPossible" : m12,
    "courseForWhom" : m13,
    "courseGradeAboveThreshold" : m14,
    "courseGradeBelowThreshold" : m15,
    "coursesLabel" : MessageLookupByLibrary.simpleMessage("Cursos"),
    "dateAtTime" : m16,
    "domainSearchHelpBody" : m17,
    "domainSearchHelpLabel" : MessageLookupByLibrary.simpleMessage("¿Cómo encuentro mi escuela o distrito?"),
    "domainSearchInputHint" : MessageLookupByLibrary.simpleMessage("Ingresar el nombre de la escuela o el distrito..."),
    "dueDateAtTime" : m18,
    "endMasqueradeLogoutMessage" : m19,
    "endMasqueradeMessage" : m20,
    "eventSubjectMessage" : m21,
    "eventTime" : m22,
    "expand" : MessageLookupByLibrary.simpleMessage("expandir"),
    "expanded" : MessageLookupByLibrary.simpleMessage("expandido"),
    "finalGrade" : m23,
    "findSchool" : MessageLookupByLibrary.simpleMessage("Buscar escuela"),
    "frontPageSubjectMessage" : m24,
    "gradeFormatScoreOutOfPointsPossible" : m25,
    "gradesSubjectMessage" : m26,
    "latePenalty" : m27,
    "me" : MessageLookupByLibrary.simpleMessage("yo"),
    "messageLinkPostscript" : m28,
    "minus" : MessageLookupByLibrary.simpleMessage("menos"),
    "mustBeAboveN" : m29,
    "mustBeBelowN" : m30,
    "next" : MessageLookupByLibrary.simpleMessage("Siguiente"),
    "nextMonth" : m31,
    "nextWeek" : m32,
    "noDomainResults" : m33,
    "ok" : MessageLookupByLibrary.simpleMessage("Aceptar"),
    "outOfPoints" : m34,
    "plusRecipientCount" : m35,
    "pointsPossible" : m36,
    "previousMonth" : m37,
    "previousWeek" : m38,
    "selectedMonthLabel" : m39,
    "send" : MessageLookupByLibrary.simpleMessage("enviar"),
    "submissionStatusSuccessSubtitle" : m40,
    "syllabusSubjectMessage" : m41,
    "unread" : MessageLookupByLibrary.simpleMessage("no leído"),
    "unreadCount" : m42
  };
}
