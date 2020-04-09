// DO NOT EDIT. This is code generated via package:intl/generate_localized.dart
// This is a library that provides messages for a pt_PT locale. All the
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
  String get localeName => 'pt_PT';

  static m0(version) => "v. ${version}";

  static m1(threshold) => "Classificação de atribuição acima ${threshold}";

  static m2(threshold) => "Classificação de atribuição abaixo ${threshold}";

  static m3(moduleName) => "Sua tarefa está bloqueada pelo módulo “${moduleName}”.";

  static m4(studentName, assignmentName) => "Em relação a: ${studentName}, Tarefa - ${assignmentName}";

  static m5(points) => "${points} pts";

  static m6(points) => "${points} pontos";

  static m7(authorName, howMany) => "${Intl.plural(howMany, one: '${authorName} para 1 outro', other: '${authorName} para ${howMany} outros')}";

  static m8(authorName, recipientName) => "${authorName} a ${recipientName}";

  static m9(authorName, recipientName, howMany) => "${Intl.plural(howMany, one: '${authorName} para ${recipientName} & 1 outro', other: '${authorName} para ${recipientName} & ${howMany} outros')}";

  static m10(count) => "${count}+";

  static m11(score, pointsPossible) => "${score} de ${pointsPossible} pontos";

  static m12(studentShortName) => "para ${studentShortName}";

  static m13(threshold) => "Grau da Disciplina Acima ${threshold}";

  static m14(threshold) => "Grau da Disciplina Abaixo ${threshold}";

  static m15(date, time) => "${date} em ${time}";

  static m16(canvasGuides, canvasSupport) => "Tente procurar o nome da escola ou distrito que você está a tentar aceder, como “Smith Private School” ou “Smith County Schools”. Você também pode entrar diretamente em um domínio do Canvas , como “smith.instructure.com.”\n\nPara mais informações sobre como encontrar a conta do Canvas da sua instituição, você pode visitar o ${canvasGuides}, alcançar a ${canvasSupport}, ou contatar a sua escola para obter assistência.";

  static m17(date, time) => "Termina ${date} a ${time}";

  static m18(studentName, eventTitle) => "Em relação a: ${studentName}, Evento - ${eventTitle}";

  static m19(startAt, endAt) => "${startAt} - ${endAt}";

  static m20(grade) => "Nota final: ${grade}";

  static m21(studentName) => "Em relação a: ${studentName}, Primeira página";

  static m22(score, pointsPossible) => "${score} / ${pointsPossible}";

  static m23(studentName) => "Em relação a: ${studentName}, Classificações";

  static m24(pointsLost) => "Penalidade tardia (-${pointsLost})";

  static m25(studentName, linkUrl) => "Em relação a: ${studentName}, ${linkUrl}";

  static m26(percentage) => "Deve estar acima de ${percentage}";

  static m27(percentage) => "Deve estar abaixo ${percentage}";

  static m28(month) => "Próximo mês: ${month}";

  static m29(date) => "A partir da próxima semana ${date}";

  static m30(query) => "Incapaz de encontrar escolas iguais “${query}”";

  static m31(points, howMany) => "${Intl.plural(howMany, one: 'De 1 ponto', other: 'Fora de ${points} pontos')}";

  static m32(count) => "+${count}";

  static m33(points) => "${points} ponto possível";

  static m34(month) => "Mês anterior: ${month}";

  static m35(date) => "A partir da semana anterior ${date}";

  static m36(month) => "Mês de ${month}";

  static m37(date, time) => "Esta tarefa foi submetida em ${date} em ${time} e está à espera de ser classificada";

  static m38(studentName) => "Em relação a: ${studentName}, Programa";

  static m39(count) => "${count} não lida";

  final messages = _notInlinedMessages(_notInlinedMessages);
  static _notInlinedMessages(_) => <String, Function> {
    "-" : MessageLookupByLibrary.simpleMessage("-"),
    "A description is required." : MessageLookupByLibrary.simpleMessage("Uma descrição é necessária."),
    "A subject is required." : MessageLookupByLibrary.simpleMessage("É necessário um sujeito."),
    "Add Student" : MessageLookupByLibrary.simpleMessage("Adicionar aluno"),
    "Add attachment" : MessageLookupByLibrary.simpleMessage("Adicionar Anexo"),
    "Add new student" : MessageLookupByLibrary.simpleMessage("Adicionar novo aluno"),
    "Add student with…" : MessageLookupByLibrary.simpleMessage("Adicionar aluno com..."),
    "Alert Settings" : MessageLookupByLibrary.simpleMessage("Definições de Alerta"),
    "Alert me when…" : MessageLookupByLibrary.simpleMessage("Alerte-me quando..."),
    "All Grading Periods" : MessageLookupByLibrary.simpleMessage("Todos os períodos de classificação"),
    "An email address is required." : MessageLookupByLibrary.simpleMessage("É necessário um endereço de e-mail."),
    "An error occurred when trying to display this link" : MessageLookupByLibrary.simpleMessage("Ocorreu um erro ao tentar exibir esta ligação"),
    "An unexpected error occurred" : MessageLookupByLibrary.simpleMessage("Ocorreu um erro inesperado"),
    "Android OS version" : MessageLookupByLibrary.simpleMessage("versão do Android OS"),
    "Appearance" : MessageLookupByLibrary.simpleMessage("Aparência"),
    "Application version" : MessageLookupByLibrary.simpleMessage("Versão da aplicação"),
    "Are you a student or teacher?" : MessageLookupByLibrary.simpleMessage("Você é um aluno ou professor?"),
    "Are you sure you want to log out?" : MessageLookupByLibrary.simpleMessage("Tem a certeza de que quer terminar a sessão?"),
    "Are you sure you wish to close this page? Your unsent message will be lost." : MessageLookupByLibrary.simpleMessage("Tem a certeza de que pretende fechar esta página? A sua mensagem não enviada será perdida."),
    "Assignment Details" : MessageLookupByLibrary.simpleMessage("Detalhes da tarefa"),
    "Assignment grade above" : MessageLookupByLibrary.simpleMessage("Classificação de atribuição acima"),
    "Assignment grade below" : MessageLookupByLibrary.simpleMessage("Classificação de atribuição abaixo"),
    "Assignment missing" : MessageLookupByLibrary.simpleMessage("Tarefa em falta"),
    "Calendars" : MessageLookupByLibrary.simpleMessage("Calendários"),
    "Cancel" : MessageLookupByLibrary.simpleMessage("Cancelar"),
    "Canvas Student" : MessageLookupByLibrary.simpleMessage("Aluno Canvas"),
    "Canvas Teacher" : MessageLookupByLibrary.simpleMessage("Canvas Teacher"),
    "Canvas on GitHub" : MessageLookupByLibrary.simpleMessage("Canvas no GitHub"),
    "Choose a course to message" : MessageLookupByLibrary.simpleMessage("Escolha uma disciplina para a mensagem"),
    "Choose from Gallery" : MessageLookupByLibrary.simpleMessage("Escolha na Galeria"),
    "Complete" : MessageLookupByLibrary.simpleMessage("Completo"),
    "Contact Support" : MessageLookupByLibrary.simpleMessage("Contactar Suporte"),
    "Course Announcement" : MessageLookupByLibrary.simpleMessage("Anúncio da disciplina"),
    "Course Announcements" : MessageLookupByLibrary.simpleMessage("Anúncios da disciplina"),
    "Course grade above" : MessageLookupByLibrary.simpleMessage("Classificação da disciplina acima"),
    "Course grade below" : MessageLookupByLibrary.simpleMessage("Classificação da disciplina abaixo"),
    "Dark Mode" : MessageLookupByLibrary.simpleMessage("Modo Escuro"),
    "Date" : MessageLookupByLibrary.simpleMessage("Data"),
    "Delete" : MessageLookupByLibrary.simpleMessage("Eliminar"),
    "Description" : MessageLookupByLibrary.simpleMessage("Descrição"),
    "Device" : MessageLookupByLibrary.simpleMessage("Dispositivo"),
    "Device model" : MessageLookupByLibrary.simpleMessage("Modelo do dispositivo"),
    "Domain:" : MessageLookupByLibrary.simpleMessage("Domínio:"),
    "Done" : MessageLookupByLibrary.simpleMessage("Feito"),
    "Download" : MessageLookupByLibrary.simpleMessage("Descarregar"),
    "Due" : MessageLookupByLibrary.simpleMessage("Vencimento"),
    "EXTREME CRITICAL EMERGENCY!!" : MessageLookupByLibrary.simpleMessage("EMERGÊNCIA EXTREMA CRÍTICA !!"),
    "Email Address" : MessageLookupByLibrary.simpleMessage("Endereço de e-mail"),
    "Email:" : MessageLookupByLibrary.simpleMessage("Email:"),
    "Enter the student pairing code provided to you. If the pairing code doesn\'t work, it may have expired" : MessageLookupByLibrary.simpleMessage("Introduza o código de pareamento de alunos que lhe foi fornecido. Se o código de pareamento não funcionar, ele pode ter expirado"),
    "Event" : MessageLookupByLibrary.simpleMessage("Evento"),
    "Excused" : MessageLookupByLibrary.simpleMessage("Desculpado"),
    "Failed. Tap for options." : MessageLookupByLibrary.simpleMessage("Falhou. Toque para opções."),
    "Filter" : MessageLookupByLibrary.simpleMessage("Filtrar"),
    "Filter by" : MessageLookupByLibrary.simpleMessage("Filtrar por"),
    "Front Page" : MessageLookupByLibrary.simpleMessage("Primeira página"),
    "Full error message" : MessageLookupByLibrary.simpleMessage("Mensagem de erro completa"),
    "Grade" : MessageLookupByLibrary.simpleMessage("Nota"),
    "Grade percentage" : MessageLookupByLibrary.simpleMessage("Classificação percentual"),
    "Graded" : MessageLookupByLibrary.simpleMessage("Classificado"),
    "Grades" : MessageLookupByLibrary.simpleMessage("Classificações"),
    "Help" : MessageLookupByLibrary.simpleMessage("Ajuda"),
    "High Contrast Mode" : MessageLookupByLibrary.simpleMessage("Modo de Alto Contraste"),
    "How is this affecting you?" : MessageLookupByLibrary.simpleMessage("Como é que isto o afeta?"),
    "I can\'t get things done until I hear back from you." : MessageLookupByLibrary.simpleMessage("Não consigo terminar as tarefas até receber indicações vossas."),
    "I need some help but it\'s not urgent." : MessageLookupByLibrary.simpleMessage("Preciso de ajuda mas não é urgente."),
    "I\'m having trouble logging in" : MessageLookupByLibrary.simpleMessage("Estou a ter problemas para fazer login"),
    "Idea for Canvas Parent App [Android]" : MessageLookupByLibrary.simpleMessage("Ideia para a App Canvas Parent [Android]"),
    "Inbox" : MessageLookupByLibrary.simpleMessage("Caixa de entrada"),
    "Inbox Zero" : MessageLookupByLibrary.simpleMessage("Caixa de mensagem Zero"),
    "Incomplete" : MessageLookupByLibrary.simpleMessage("Incompleto"),
    "Institution Announcement" : MessageLookupByLibrary.simpleMessage("Anúncio da Instituição"),
    "Institution Announcements" : MessageLookupByLibrary.simpleMessage("Anúncios da Instituição"),
    "Instructions" : MessageLookupByLibrary.simpleMessage("Instruções"),
    "It looks like a great day to rest, relax, and recharge." : MessageLookupByLibrary.simpleMessage("Parece um ótimo dia para descansar, relaxar e recarregar."),
    "It looks like assignments haven\'t been created in this space yet." : MessageLookupByLibrary.simpleMessage("Parece que as tarefas ainda não foram criadas neste espaço."),
    "Just a casual question, comment, idea, suggestion…" : MessageLookupByLibrary.simpleMessage("Apenas uma questão casual, comentário, ideia, sugestão..."),
    "Late" : MessageLookupByLibrary.simpleMessage("Atrasado"),
    "Launch External Tool" : MessageLookupByLibrary.simpleMessage("Lançar Ferramenta Externa"),
    "Legal" : MessageLookupByLibrary.simpleMessage("Legal"),
    "Light Mode" : MessageLookupByLibrary.simpleMessage("Modo de luz"),
    "Link Error" : MessageLookupByLibrary.simpleMessage("Erro na ligação"),
    "Locale:" : MessageLookupByLibrary.simpleMessage("Local:"),
    "Location" : MessageLookupByLibrary.simpleMessage("Local"),
    "Locked" : MessageLookupByLibrary.simpleMessage("Bloqueado"),
    "Log Out" : MessageLookupByLibrary.simpleMessage("Sair"),
    "Manage Students" : MessageLookupByLibrary.simpleMessage("Gerir alunos"),
    "Message" : MessageLookupByLibrary.simpleMessage("Mensagem"),
    "Message subject" : MessageLookupByLibrary.simpleMessage("Assunto da mensagem"),
    "Missing" : MessageLookupByLibrary.simpleMessage("Em falta"),
    "Must be below 100" : MessageLookupByLibrary.simpleMessage("Deve estar abaixo de 100"),
    "Network error" : MessageLookupByLibrary.simpleMessage("Erro de rede"),
    "Never" : MessageLookupByLibrary.simpleMessage("Nunca"),
    "New message" : MessageLookupByLibrary.simpleMessage("Nova mensagem"),
    "No" : MessageLookupByLibrary.simpleMessage("Não"),
    "No Alerts" : MessageLookupByLibrary.simpleMessage("Sem Alertas"),
    "No Assignments" : MessageLookupByLibrary.simpleMessage("Nenhuma tarefa"),
    "No Courses" : MessageLookupByLibrary.simpleMessage("Sem Disciplinas"),
    "No Due Date" : MessageLookupByLibrary.simpleMessage("Sem data de vencimento"),
    "No Events Today!" : MessageLookupByLibrary.simpleMessage("Nenhum evento hoje!"),
    "No Grade" : MessageLookupByLibrary.simpleMessage("Sem classificação"),
    "No Location Specified" : MessageLookupByLibrary.simpleMessage("Sem Local Especificado"),
    "No Students" : MessageLookupByLibrary.simpleMessage("Não existem alunos"),
    "No Subject" : MessageLookupByLibrary.simpleMessage("Sem assunto"),
    "No Summary" : MessageLookupByLibrary.simpleMessage("Nenhum Resumo"),
    "No description" : MessageLookupByLibrary.simpleMessage("Sem descrição"),
    "No recipients selected" : MessageLookupByLibrary.simpleMessage("Nenhum beneficiário selecionado"),
    "Not Graded" : MessageLookupByLibrary.simpleMessage("Sem classificação"),
    "Not Submitted" : MessageLookupByLibrary.simpleMessage("Não Entregue"),
    "Not a parent?" : MessageLookupByLibrary.simpleMessage("Não é um pai?"),
    "Notifications for reminders about assignments and calendar events" : MessageLookupByLibrary.simpleMessage("Notificações para lembretes sobre tarefas e eventos do calendário"),
    "OS Version" : MessageLookupByLibrary.simpleMessage("Versão SO"),
    "Observer" : MessageLookupByLibrary.simpleMessage("Observador"),
    "One of our other apps might be a better fit. Tap one to visit the Play Store." : MessageLookupByLibrary.simpleMessage("Uma das nossas outras aplicações pode ser um ajuste melhor. Toque em um para visitar a App Store."),
    "Open In Browser" : MessageLookupByLibrary.simpleMessage("Abrir no Navegador"),
    "Open with another app" : MessageLookupByLibrary.simpleMessage("Abrir com outra aplicação"),
    "Pairing Code" : MessageLookupByLibrary.simpleMessage("Código de pareamento"),
    "Preparing…" : MessageLookupByLibrary.simpleMessage("A preparar..."),
    "Previous Logins" : MessageLookupByLibrary.simpleMessage("Logins anteriores"),
    "Privacy Policy" : MessageLookupByLibrary.simpleMessage("Política de Privacidade"),
    "Privacy policy, terms of use, open source" : MessageLookupByLibrary.simpleMessage("Política de privacidade, termos de uso, código aberto"),
    "QR Code" : MessageLookupByLibrary.simpleMessage("Código QR"),
    "Recipients" : MessageLookupByLibrary.simpleMessage("Destinatários"),
    "Remind Me" : MessageLookupByLibrary.simpleMessage("Relembra-me"),
    "Reminders" : MessageLookupByLibrary.simpleMessage("Lembretes"),
    "Reply" : MessageLookupByLibrary.simpleMessage("Responder"),
    "Reply All" : MessageLookupByLibrary.simpleMessage("Responder a todos"),
    "Report A Problem" : MessageLookupByLibrary.simpleMessage("Relatar um Problema"),
    "Request Login Help" : MessageLookupByLibrary.simpleMessage("Solicitar Ajuda de Login"),
    "Request Login Help Button" : MessageLookupByLibrary.simpleMessage("Solicitar botão de Ajuda de Login"),
    "Restart app" : MessageLookupByLibrary.simpleMessage("Reiniciar aplicação"),
    "Retry" : MessageLookupByLibrary.simpleMessage("Tentar novamente"),
    "Return to Login" : MessageLookupByLibrary.simpleMessage("Voltar para Login"),
    "STUDENT" : MessageLookupByLibrary.simpleMessage("ALUNO"),
    "Select recipients" : MessageLookupByLibrary.simpleMessage("Selecione Destinatários"),
    "Send a message about this assignment" : MessageLookupByLibrary.simpleMessage("Enviar uma mensagem sobre esta tarefa"),
    "Send a message about this course" : MessageLookupByLibrary.simpleMessage("Enviar uma mensagem a cerca dessa disciplina"),
    "Send message" : MessageLookupByLibrary.simpleMessage("Enviar mensagem"),
    "Set a date and time to be notified of this event." : MessageLookupByLibrary.simpleMessage("Defina uma data e hora para ser notificado deste evento."),
    "Set a date and time to be notified of this specific assignment." : MessageLookupByLibrary.simpleMessage("Defina uma data e hora para ser notificado sobre esta atribuição específica."),
    "Set reminder switch" : MessageLookupByLibrary.simpleMessage("Definir interruptor de lembrete"),
    "Settings" : MessageLookupByLibrary.simpleMessage("Definições"),
    "Share Your Love for the App" : MessageLookupByLibrary.simpleMessage("Partilhe o Seu Carinho pela App"),
    "Something\'s broken but I can work around it to get what I need done." : MessageLookupByLibrary.simpleMessage("Algo não está bem mas consigo contornar a dificuldade e fazer o que preciso."),
    "Student" : MessageLookupByLibrary.simpleMessage("Aluno"),
    "Subject" : MessageLookupByLibrary.simpleMessage("Assunto"),
    "Submitted" : MessageLookupByLibrary.simpleMessage("Submetido"),
    "Successfully submitted!" : MessageLookupByLibrary.simpleMessage("Submetido com sucesso!"),
    "Summary" : MessageLookupByLibrary.simpleMessage("Sumário"),
    "Switch Users" : MessageLookupByLibrary.simpleMessage("Alterar Utilizadores"),
    "Syllabus" : MessageLookupByLibrary.simpleMessage("Programa"),
    "TA" : MessageLookupByLibrary.simpleMessage("Assistente"),
    "TEACHER" : MessageLookupByLibrary.simpleMessage("PROFESSOR"),
    "Tap to favorite the courses you want to see on the Calendar." : MessageLookupByLibrary.simpleMessage("Toque para escolher os percursos que deseja ver no Calendário."),
    "Tap to pair with a new student" : MessageLookupByLibrary.simpleMessage("Toque para parelhar com um novo aluno"),
    "Tap to select this student" : MessageLookupByLibrary.simpleMessage("Toque para selecionar este aluno"),
    "Tap to show student selector" : MessageLookupByLibrary.simpleMessage("Toque para mostrar o seletor de alunos"),
    "Teacher" : MessageLookupByLibrary.simpleMessage("Professor"),
    "Tell us about your favorite parts of the app" : MessageLookupByLibrary.simpleMessage("Diga-nos quais são as suas partes favoritas da app"),
    "Terms of Use" : MessageLookupByLibrary.simpleMessage("Termos de uso"),
    "The following information will help us better understand your idea:" : MessageLookupByLibrary.simpleMessage("A seguinte informação vai ajudar-nos a compreender melhora sua ideia:"),
    "The server you entered is not authorized for this app." : MessageLookupByLibrary.simpleMessage("O servidor que você digitou não está autorizado para esta app."),
    "The user agent for this app is not authorized." : MessageLookupByLibrary.simpleMessage("O agente do utilizador para esta app não está autorizado."),
    "Theme" : MessageLookupByLibrary.simpleMessage("Tema"),
    "There are no installed applications that can open this file" : MessageLookupByLibrary.simpleMessage("Não existem aplicações instaladas que possam abrir este ficheiro"),
    "There is no page information available." : MessageLookupByLibrary.simpleMessage("Não existe informação de página disponível."),
    "There was a problem loading the Terms of Use" : MessageLookupByLibrary.simpleMessage("Ocorreu um problema ao carregar os Termos de Uso"),
    "There was an error loading recipients for this course" : MessageLookupByLibrary.simpleMessage("Houve um erro ao carregar os destinatários para esta disciplina"),
    "There was an error loading the summary details for this course." : MessageLookupByLibrary.simpleMessage("Houve um erro ao carregar os detalhes resumidos para esta disciplina."),
    "There was an error loading this announcement" : MessageLookupByLibrary.simpleMessage("Houve um erro ao carregar este anúncio"),
    "There was an error loading this conversation" : MessageLookupByLibrary.simpleMessage("Ocorreu um erro ao carregar esta conversa"),
    "There was an error loading this file" : MessageLookupByLibrary.simpleMessage("Houve um erro ao carregar este ficheiro"),
    "There was an error loading your inbox messages." : MessageLookupByLibrary.simpleMessage("Houve um erro ao carregar as mensagens da sua caixa de entrada."),
    "There was an error loading your student\'s alerts." : MessageLookupByLibrary.simpleMessage("Houve um erro ao carregar os alertas dos seus alunos."),
    "There was an error loading your student\'s calendar" : MessageLookupByLibrary.simpleMessage("Houve um erro ao carregar o calendário do seu aluno."),
    "There was an error loading your students." : MessageLookupByLibrary.simpleMessage("Houve um erro ao carregar os seus alunos."),
    "There was an error loading your your student’s courses." : MessageLookupByLibrary.simpleMessage("Houve um erro ao carregar as disciplinas dos alunos."),
    "There’s nothing to be notified of yet." : MessageLookupByLibrary.simpleMessage("Não há nada para ser notificado ainda."),
    "This app is not authorized for use." : MessageLookupByLibrary.simpleMessage("Esta app não está autorizada para utilização."),
    "This course does not have any assignments or calendar events yet." : MessageLookupByLibrary.simpleMessage("Esta disciplina ainda não tem nenhuma tarefa ou calendário de eventos."),
    "This file is unsupported and can’t be viewed through the app" : MessageLookupByLibrary.simpleMessage("Este ficheiro não tem suporte e não pode ser visualizado através da aplicação"),
    "Total Grade" : MessageLookupByLibrary.simpleMessage("Nota Completa"),
    "Uh oh!" : MessageLookupByLibrary.simpleMessage("Ah não!"),
    "Unable to fetch courses. Please check your connection and try again." : MessageLookupByLibrary.simpleMessage("Incapaz de ir buscar disciplinas. É favor verificar sua conexão e tente novamente."),
    "Unable to load this image" : MessageLookupByLibrary.simpleMessage("Incapaz de carregar esta imagem"),
    "Unable to play this media file" : MessageLookupByLibrary.simpleMessage("Incapaz de reproduzir este ficheiro multimédia"),
    "Unable to send message. Check your connection and try again." : MessageLookupByLibrary.simpleMessage("Incapaz de enviar mensagem. Verifique a sua ligação e tente novamente."),
    "Under Construction" : MessageLookupByLibrary.simpleMessage("Em construção"),
    "Unknown User" : MessageLookupByLibrary.simpleMessage("Utilizador desconhecido"),
    "Unsaved changes" : MessageLookupByLibrary.simpleMessage("Alterações não guardadas"),
    "Unsupported File" : MessageLookupByLibrary.simpleMessage("Ficheiro não suportado"),
    "Upload File" : MessageLookupByLibrary.simpleMessage("Carregar ficheiro"),
    "Use Camera" : MessageLookupByLibrary.simpleMessage("Use a câmara"),
    "User ID:" : MessageLookupByLibrary.simpleMessage("ID do Utilizador:"),
    "Version Number" : MessageLookupByLibrary.simpleMessage("Número da versão"),
    "View error details" : MessageLookupByLibrary.simpleMessage("Ver detalhes do erro"),
    "We are currently building this feature for your viewing pleasure." : MessageLookupByLibrary.simpleMessage("Estamos atualmente a construir esta funcionalidade para o seu prazer visual."),
    "We are unable to display this link, it may belong to an institution you currently aren\'t logged in to." : MessageLookupByLibrary.simpleMessage("Não podemos exibir esta ligação, ele pode pertencer a uma instituição na qual você não está atualmente conectado."),
    "We couldn\'t find any students associated with this account" : MessageLookupByLibrary.simpleMessage("Não conseguimos encontrar nenhum estudante associado a esta conta"),
    "We were unable to verify the server for use with this app." : MessageLookupByLibrary.simpleMessage("Não conseguimos verificar o servidor para utilização com esta app."),
    "We’re not sure what happened, but it wasn’t good. Contact us if this keeps happening." : MessageLookupByLibrary.simpleMessage("Nós não temos certeza do que aconteceu, mas não foi bom. Contacte-nos se isto continuar a acontecer."),
    "Yes" : MessageLookupByLibrary.simpleMessage("Sim"),
    "You are not observing any students." : MessageLookupByLibrary.simpleMessage("Você não está a observar nenhum aluno."),
    "You will be notified about this assignment on…" : MessageLookupByLibrary.simpleMessage("Você será notificado sobre esta atribuição em..."),
    "You will be notified about this event on…" : MessageLookupByLibrary.simpleMessage("Você será notificado sobre este evento em..."),
    "Your code is incorrect or expired." : MessageLookupByLibrary.simpleMessage("O seu código está incorrecto ou expirou."),
    "Your student’s courses might not be published yet." : MessageLookupByLibrary.simpleMessage("As disciplinas dos seus alunos podem ainda não ter sido publicadas."),
    "You’re all caught up!" : MessageLookupByLibrary.simpleMessage("Vocês estão todos apanhados!"),
    "alertsLabel" : MessageLookupByLibrary.simpleMessage("Alertas"),
    "appVersion" : m0,
    "assignmentGradeAboveThreshold" : m1,
    "assignmentGradeBelowThreshold" : m2,
    "assignmentLockedModule" : m3,
    "assignmentSubjectMessage" : m4,
    "assignmentTotalPoints" : m5,
    "assignmentTotalPointsAccessible" : m6,
    "authorToNOthers" : m7,
    "authorToRecipient" : m8,
    "authorToRecipientAndNOthers" : m9,
    "badgeNumberPlus" : m10,
    "calendarLabel" : MessageLookupByLibrary.simpleMessage("Calendário"),
    "canvasGuides" : MessageLookupByLibrary.simpleMessage("Guias Canvas"),
    "canvasLogoLabel" : MessageLookupByLibrary.simpleMessage("Logo Canvas"),
    "canvasSupport" : MessageLookupByLibrary.simpleMessage("Suporte Canvas"),
    "collapse" : MessageLookupByLibrary.simpleMessage("recolhido"),
    "collapsed" : MessageLookupByLibrary.simpleMessage("recolhido"),
    "contentDescriptionScoreOutOfPointsPossible" : m11,
    "courseForWhom" : m12,
    "courseGradeAboveThreshold" : m13,
    "courseGradeBelowThreshold" : m14,
    "coursesLabel" : MessageLookupByLibrary.simpleMessage("Disciplinas"),
    "dateAtTime" : m15,
    "dismiss" : MessageLookupByLibrary.simpleMessage("ignorar"),
    "domainSearchHelpBody" : m16,
    "domainSearchHelpLabel" : MessageLookupByLibrary.simpleMessage("Como posso encontrar a minha escola ou distrito?"),
    "domainSearchInputHint" : MessageLookupByLibrary.simpleMessage("Digite o nome da escola ou distrito..."),
    "dueDateAtTime" : m17,
    "eventSubjectMessage" : m18,
    "eventTime" : m19,
    "expand" : MessageLookupByLibrary.simpleMessage("expandido"),
    "expanded" : MessageLookupByLibrary.simpleMessage("expandido"),
    "finalGrade" : m20,
    "findSchool" : MessageLookupByLibrary.simpleMessage("Encontrar Escola"),
    "frontPageSubjectMessage" : m21,
    "gradeFormatScoreOutOfPointsPossible" : m22,
    "gradesSubjectMessage" : m23,
    "latePenalty" : m24,
    "me" : MessageLookupByLibrary.simpleMessage("eu"),
    "messageLinkPostscript" : m25,
    "minus" : MessageLookupByLibrary.simpleMessage("menos"),
    "mustBeAboveN" : m26,
    "mustBeBelowN" : m27,
    "next" : MessageLookupByLibrary.simpleMessage("Próximo"),
    "nextMonth" : m28,
    "nextWeek" : m29,
    "noDomainResults" : m30,
    "ok" : MessageLookupByLibrary.simpleMessage("OK"),
    "outOfPoints" : m31,
    "plusRecipientCount" : m32,
    "pointsPossible" : m33,
    "previousMonth" : m34,
    "previousWeek" : m35,
    "selectedMonthLabel" : m36,
    "send" : MessageLookupByLibrary.simpleMessage("enviar"),
    "submissionStatusSuccessSubtitle" : m37,
    "syllabusSubjectMessage" : m38,
    "unread" : MessageLookupByLibrary.simpleMessage("não lida"),
    "unreadCount" : m39
  };
}
