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

  static m0(userName) => "Você está a agir como ${userName}";

  static m1(version) => "v. ${version}";

  static m2(threshold) => "Classificação de atribuição acima ${threshold}";

  static m3(threshold) => "Classificação de atribuição abaixo ${threshold}";

  static m4(moduleName) => "Sua tarefa está bloqueada pelo módulo “${moduleName}”.";

  static m5(studentName, assignmentName) => "Em relação a: ${studentName}, Tarefa - ${assignmentName}";

  static m6(points) => "${points} pts";

  static m7(points) => "${points} pontos";

  static m8(authorName, howMany) => "${Intl.plural(howMany, one: '${authorName} para 1 outro', other: '${authorName} para ${howMany} outros')}";

  static m9(authorName, recipientName) => "${authorName} a ${recipientName}";

  static m10(authorName, recipientName, howMany) => "${Intl.plural(howMany, one: '${authorName} para ${recipientName} & 1 outro', other: '${authorName} para ${recipientName} & ${howMany} outros')}";

  static m11(count) => "${count}+";

  static m12(score, pointsPossible) => "${score} de ${pointsPossible} pontos";

  static m13(studentShortName) => "para ${studentShortName}";

  static m14(threshold) => "Grau da Disciplina Acima ${threshold}";

  static m15(threshold) => "Grau da Disciplina Abaixo ${threshold}";

  static m16(date, time) => "${date} em ${time}";

  static m17(canvasGuides, canvasSupport) => "Tente procurar o nome da escola ou distrito que você está a tentar aceder, como “Smith Private School” ou “Smith County Schools”. Você também pode entrar diretamente em um domínio do Canvas , como “smith.instructure.com.”\n\nPara mais informações sobre como encontrar a conta do Canvas da sua instituição, você pode visitar o ${canvasGuides}, alcançar a ${canvasSupport}, ou contatar a sua escola para obter assistência.";

  static m18(date, time) => "Termina ${date} a ${time}";

  static m19(userName) => "Você vai deixar de agir como ${userName} e será desconectado.";

  static m20(userName) => "Vai deixar de agir como ${userName} e voltar à sua conta original.";

  static m21(studentName, eventTitle) => "Em relação a: ${studentName}, Evento - ${eventTitle}";

  static m22(startAt, endAt) => "${startAt} - ${endAt}";

  static m23(grade) => "Nota final: ${grade}";

  static m24(studentName) => "Em relação a: ${studentName}, Primeira página";

  static m25(score, pointsPossible) => "${score} / ${pointsPossible}";

  static m26(studentName) => "Em relação a: ${studentName}, Classificações";

  static m27(pointsLost) => "Penalidade tardia (-${pointsLost})";

  static m28(studentName, linkUrl) => "Em relação a: ${studentName}, ${linkUrl}";

  static m29(percentage) => "Deve estar acima de ${percentage}";

  static m30(percentage) => "Deve estar abaixo ${percentage}";

  static m31(month) => "Próximo mês: ${month}";

  static m32(date) => "A partir da próxima semana ${date}";

  static m33(query) => "Incapaz de encontrar escolas iguais “${query}”";

  static m34(points, howMany) => "${Intl.plural(howMany, one: 'De 1 ponto', other: 'Fora de ${points} pontos')}";

  static m35(count) => "+${count}";

  static m36(points) => "${points} ponto possível";

  static m37(month) => "Mês anterior: ${month}";

  static m38(date) => "A partir da semana anterior ${date}";

  static m39(month) => "Mês de ${month}";

  static m40(date, time) => "Esta tarefa foi submetida em ${date} em ${time} e está à espera de ser classificada";

  static m41(studentName) => "Em relação a: ${studentName}, Programa";

  static m42(count) => "${count} não lida";

  final messages = _notInlinedMessages(_notInlinedMessages);
  static _notInlinedMessages(_) => <String, Function> {
    "\"Act as\" is essentially logging in as this user without a password. You will be able to take any action as if you were this user, and from other users\' points of views, it will be as if this user performed them. However, audit logs record that you were the one who performed the actions on behalf of this user." : MessageLookupByLibrary.simpleMessage("\"Atuar como\" é essencialmente fazer logon como esse utilizador sem uma palavra passe. Você será capaz de executar qualquer ação como se fosse este utilizador e de pontos de vista de outros utilizadores, será como se este utilizador executou-os. No entanto, registos de auditoria registam que você foi o único que realizou as ações em nome desse utilizador."),
    "-" : MessageLookupByLibrary.simpleMessage("-"),
    "A description is required." : MessageLookupByLibrary.simpleMessage("Uma descrição é necessária."),
    "A subject is required." : MessageLookupByLibrary.simpleMessage("É necessário um sujeito."),
    "Act As User" : MessageLookupByLibrary.simpleMessage("Agir como Utilizador"),
    "Add Student" : MessageLookupByLibrary.simpleMessage("Adicionar aluno"),
    "Add attachment" : MessageLookupByLibrary.simpleMessage("Adicionar Anexo"),
    "Add new student" : MessageLookupByLibrary.simpleMessage("Adicionar novo aluno"),
    "Add student with…" : MessageLookupByLibrary.simpleMessage("Adicionar aluno com..."),
    "Alert Settings" : MessageLookupByLibrary.simpleMessage("Definições de Alerta"),
    "Alert me when…" : MessageLookupByLibrary.simpleMessage("Alerte-me quando..."),
    "All Grading Periods" : MessageLookupByLibrary.simpleMessage("Todos os períodos de classificação"),
    "An email address is required." : MessageLookupByLibrary.simpleMessage("É necessário um endereço de e-mail."),
    "An error occurred when trying to display this link" : MessageLookupByLibrary.simpleMessage("Ocorreu um erro ao tentar mostrar esta ligação"),
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
    "Domain" : MessageLookupByLibrary.simpleMessage("Domínio"),
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
    "Interactions on this page are limited by your institution." : MessageLookupByLibrary.simpleMessage("As interações nesta página são limitadas pela sua instituição."),
    "It looks like a great day to rest, relax, and recharge." : MessageLookupByLibrary.simpleMessage("Parece um ótimo dia para descansar, relaxar e recarregar."),
    "It looks like assignments haven\'t been created in this space yet." : MessageLookupByLibrary.simpleMessage("Parece que as tarefas ainda não foram criadas neste espaço."),
    "Just a casual question, comment, idea, suggestion…" : MessageLookupByLibrary.simpleMessage("Apenas uma questão casual, comentário, ideia, sugestão..."),
    "Late" : MessageLookupByLibrary.simpleMessage("Atrasado"),
    "Launch External Tool" : MessageLookupByLibrary.simpleMessage("Lançar Ferramenta Externa"),
    "Legal" : MessageLookupByLibrary.simpleMessage("Legal"),
    "Light Mode" : MessageLookupByLibrary.simpleMessage("Modo de luz"),
    "Link Error" : MessageLookupByLibrary.simpleMessage("Erro de Ligação"),
    "Locale:" : MessageLookupByLibrary.simpleMessage("Local:"),
    "Location" : MessageLookupByLibrary.simpleMessage("Local"),
    "Locked" : MessageLookupByLibrary.simpleMessage("Bloqueado"),
    "Log Out" : MessageLookupByLibrary.simpleMessage("Sair"),
    "Login flow: Canvas" : MessageLookupByLibrary.simpleMessage("Fluxo de login: Canvas"),
    "Login flow: Normal" : MessageLookupByLibrary.simpleMessage("Fluxo de login: Normal"),
    "Login flow: Site Admin" : MessageLookupByLibrary.simpleMessage("Fluxo de login: Administrador do site"),
    "Login flow: Skip mobile verify" : MessageLookupByLibrary.simpleMessage("Fluxo de login: Saltar verificação móvel"),
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
    "Settings" : MessageLookupByLibrary.simpleMessage("Definições"),
    "Share Your Love for the App" : MessageLookupByLibrary.simpleMessage("Partilhe o Seu Carinho pela App"),
    "Something\'s broken but I can work around it to get what I need done." : MessageLookupByLibrary.simpleMessage("Algo não está bem mas consigo contornar a dificuldade e fazer o que preciso."),
    "Stop Acting as User" : MessageLookupByLibrary.simpleMessage("Parar de atuar como utilizador"),
    "Student" : MessageLookupByLibrary.simpleMessage("Aluno"),
    "Subject" : MessageLookupByLibrary.simpleMessage("Assunto"),
    "Submitted" : MessageLookupByLibrary.simpleMessage("Submetido"),
    "Successfully submitted!" : MessageLookupByLibrary.simpleMessage("Submetido com sucesso!"),
    "Summary" : MessageLookupByLibrary.simpleMessage("Sumário"),
    "Switch Users" : MessageLookupByLibrary.simpleMessage("Alterar Utilizadores"),
    "Syllabus" : MessageLookupByLibrary.simpleMessage("Programa"),
    "TA" : MessageLookupByLibrary.simpleMessage("Assistente"),
    "TEACHER" : MessageLookupByLibrary.simpleMessage("PROFESSOR"),
    "Tap to favorite the courses you want to see on the Calendar." : MessageLookupByLibrary.simpleMessage("Toque para escolher os percursos que pretende ver no Calendário."),
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
    "There was an error trying to act as this user. Please check the Domain and User ID and try again." : MessageLookupByLibrary.simpleMessage("Houve um erro ao tentar agir como este utilizador. Por favor, verifique o Domínio e o ID do Utilizador e tente novamente."),
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
    "User ID" : MessageLookupByLibrary.simpleMessage("ID do utilizador"),
    "User ID:" : MessageLookupByLibrary.simpleMessage("ID do Utilizador:"),
    "Version Number" : MessageLookupByLibrary.simpleMessage("Número da versão"),
    "View error details" : MessageLookupByLibrary.simpleMessage("Ver detalhes do erro"),
    "We are currently building this feature for your viewing pleasure." : MessageLookupByLibrary.simpleMessage("Estamos atualmente a construir esta funcionalidade para o seu prazer visual."),
    "We are unable to display this link, it may belong to an institution you currently aren\'t logged in to." : MessageLookupByLibrary.simpleMessage("Não podemos mostrar esta ligação, ela pode pertencer a uma instituição na qual você não está atualmente ligado."),
    "We couldn\'t find any students associated with this account" : MessageLookupByLibrary.simpleMessage("Não conseguimos encontrar nenhum estudante associado a esta conta"),
    "We were unable to verify the server for use with this app." : MessageLookupByLibrary.simpleMessage("Não conseguimos verificar o servidor para utilização com esta app."),
    "We’re not sure what happened, but it wasn’t good. Contact us if this keeps happening." : MessageLookupByLibrary.simpleMessage("Nós não temos certeza do que aconteceu, mas não foi bom. Contacte-nos se isto continuar a acontecer."),
    "Yes" : MessageLookupByLibrary.simpleMessage("Sim"),
    "You are not observing any students." : MessageLookupByLibrary.simpleMessage("Você não está a observar nenhum aluno."),
    "You must enter a user id" : MessageLookupByLibrary.simpleMessage("É necessário introduzir uma id do utilizador"),
    "You must enter a valid domain" : MessageLookupByLibrary.simpleMessage("É necessário introduzir um domínio válido"),
    "You will be notified about this assignment on…" : MessageLookupByLibrary.simpleMessage("Você será notificado sobre esta atribuição em..."),
    "You will be notified about this event on…" : MessageLookupByLibrary.simpleMessage("Você será notificado sobre este evento em..."),
    "Your code is incorrect or expired." : MessageLookupByLibrary.simpleMessage("O seu código está incorrecto ou expirou."),
    "Your student’s courses might not be published yet." : MessageLookupByLibrary.simpleMessage("As disciplinas dos seus alunos podem ainda não ter sido publicadas."),
    "You’re all caught up!" : MessageLookupByLibrary.simpleMessage("Vocês estão todos apanhados!"),
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
    "calendarLabel" : MessageLookupByLibrary.simpleMessage("Calendário"),
    "canvasGuides" : MessageLookupByLibrary.simpleMessage("Guias Canvas"),
    "canvasLogoLabel" : MessageLookupByLibrary.simpleMessage("Logo Canvas"),
    "canvasSupport" : MessageLookupByLibrary.simpleMessage("Suporte Canvas"),
    "collapse" : MessageLookupByLibrary.simpleMessage("recolhido"),
    "collapsed" : MessageLookupByLibrary.simpleMessage("recolhido"),
    "contentDescriptionScoreOutOfPointsPossible" : m12,
    "courseForWhom" : m13,
    "courseGradeAboveThreshold" : m14,
    "courseGradeBelowThreshold" : m15,
    "coursesLabel" : MessageLookupByLibrary.simpleMessage("Disciplinas"),
    "dateAtTime" : m16,
    "domainSearchHelpBody" : m17,
    "domainSearchHelpLabel" : MessageLookupByLibrary.simpleMessage("Como posso encontrar a minha escola ou distrito?"),
    "domainSearchInputHint" : MessageLookupByLibrary.simpleMessage("Digite o nome da escola ou distrito..."),
    "dueDateAtTime" : m18,
    "endMasqueradeLogoutMessage" : m19,
    "endMasqueradeMessage" : m20,
    "eventSubjectMessage" : m21,
    "eventTime" : m22,
    "expand" : MessageLookupByLibrary.simpleMessage("expandido"),
    "expanded" : MessageLookupByLibrary.simpleMessage("expandido"),
    "finalGrade" : m23,
    "findSchool" : MessageLookupByLibrary.simpleMessage("Encontrar Escola"),
    "frontPageSubjectMessage" : m24,
    "gradeFormatScoreOutOfPointsPossible" : m25,
    "gradesSubjectMessage" : m26,
    "latePenalty" : m27,
    "me" : MessageLookupByLibrary.simpleMessage("eu"),
    "messageLinkPostscript" : m28,
    "minus" : MessageLookupByLibrary.simpleMessage("menos"),
    "mustBeAboveN" : m29,
    "mustBeBelowN" : m30,
    "next" : MessageLookupByLibrary.simpleMessage("Próximo"),
    "nextMonth" : m31,
    "nextWeek" : m32,
    "noDomainResults" : m33,
    "ok" : MessageLookupByLibrary.simpleMessage("OK"),
    "outOfPoints" : m34,
    "plusRecipientCount" : m35,
    "pointsPossible" : m36,
    "previousMonth" : m37,
    "previousWeek" : m38,
    "selectedMonthLabel" : m39,
    "send" : MessageLookupByLibrary.simpleMessage("enviar"),
    "submissionStatusSuccessSubtitle" : m40,
    "syllabusSubjectMessage" : m41,
    "unread" : MessageLookupByLibrary.simpleMessage("não lida"),
    "unreadCount" : m42
  };
}
