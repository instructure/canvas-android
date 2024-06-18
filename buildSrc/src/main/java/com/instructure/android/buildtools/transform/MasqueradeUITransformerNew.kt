/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
//import com.android.build.api.instrumentation.AsmClassVisitorFactory
//import com.android.build.api.instrumentation.ClassContext
//import com.android.build.api.instrumentation.ClassData
//import com.android.build.api.instrumentation.InstrumentationParameters
//import org.objectweb.asm.ClassVisitor
//import org.objectweb.asm.MethodVisitor
//import org.objectweb.asm.Opcodes
//import org.objectweb.asm.commons.AdviceAdapter
//
//class MasqueradeUITransformerNew(cv: ClassVisitor, private val startingClass: String) : ClassVisitor(Opcodes.ASM9, cv) {
//
//    override fun visitMethod(
//        access: Int,
//        name: String?,
//        descriptor: String?,
//        signature: String?,
//        exceptions: Array<out String>?
//    ): MethodVisitor {
//        val mv = super.visitMethod(access, name, descriptor, signature, exceptions)
//        return if (name == "onPostCreate" && descriptor == "(Landroid/os/Bundle;)V") {
//            val postCreate = object : AdviceAdapter(Opcodes.ASM9, mv, access, name, descriptor) {
//                override fun onMethodEnter() {
//                    mv.visitLdcInsn(startingClass)
//                    mv.visitMethodInsn(
//                        Opcodes.INVOKESTATIC,
//                        "com/instructure/loginapi/login/util/MasqueradeUI",
//                        "showMasqueradeNotification",
//                        "(Landroid/app/Activity;Ljava/lang/String;)V",
//                        false
//                    )
//                }
//            }
//            postCreate
//        } else if (name == "onStart" && descriptor == "()V") {
//            object : AdviceAdapter(Opcodes.ASM9, mv, access, name, descriptor) {
//                override fun onMethodEnter() {
//                    mv.visitLdcInsn(startingClass)
//                    mv.visitMethodInsn(
//                        Opcodes.INVOKESTATIC,
//                        "com/instructure/loginapi/login/util/MasqueradeUI",
//                        "showMasqueradeNotification",
//                        "(Landroid/app/DialogFragment;Ljava/lang/String;)V",
//                        false
//                    )
//                }
//            }
//        } else {
//            mv
//        }
//    }
//}
//
//abstract class MasqueradeUIVisitorFactory : AsmClassVisitorFactory<MasqueradeUIParameters> {
//    override fun createClassVisitor(classContext: ClassContext, nextClassVisitor: ClassVisitor): ClassVisitor {
//        return MasqueradeUITransformerNew(nextClassVisitor, parameters.get().className)
//    }
//
//    override fun isInstrumentable(classData: ClassData): Boolean {
//        return classData.className.contains("instructure")
//    }
//}
//
//interface MasqueradeUIParameters : InstrumentationParameters {
//    var className: String
//}
