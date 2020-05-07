def scriptFile = getClass().protectionDomain.codeSource.location.toURI()
def flutterProjectRoot = new File(scriptFile).parentFile.parentFile

gradle.include ':flutter-student-embed'
gradle.project(':flutter-student-embed').projectDir = new File(flutterProjectRoot, 'flutter_student_embed/.android/Flutter')

def plugins = new Properties()
def pluginsFile = new File(flutterProjectRoot, 'flutter_student_embed/.flutter-plugins')
if (pluginsFile.exists()) {
    pluginsFile.withReader('UTF-8') { reader -> plugins.load(reader) }
}

plugins.each { name, path ->
    def pluginDirectory = flutterProjectRoot.toPath().resolve(path).resolve('android').toFile()
    gradle.include ":$name"
    gradle.project(":$name").projectDir = pluginDirectory
}

gradle.getGradle().projectsLoaded { g ->
    g.rootProject.beforeEvaluate { p ->
        _mainModuleName = binding.variables['mainModuleName']
        if (_mainModuleName != null && !_mainModuleName.empty) {
            p.ext.mainModuleName = _mainModuleName
        }
    }
    g.rootProject.afterEvaluate { p ->
        p.subprojects { sp ->
            if (sp.name == 'student') {
                sp.evaluationDependsOn(':flutter-student-embed')
            }
        }
    }
}
