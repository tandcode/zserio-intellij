package org.zserio.intellij.settings

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import org.zserio.intellij.version.ZserioSupport

class ZserioConfigurable : BoundConfigurable("Zserio") {
    override fun createPanel(): DialogPanel {
        val state = ZserioSettings.getInstance().state
        return panel {
            row {
                comment(
                    "Supported Zserio versions: <b>${ZserioSupport.RANGE_TEXT}</b>. Nothing needs to be configured " +
                        "for them, the plugin understands the syntax of all of them."
                )
            }
            group("Compiler Diagnostics") {
                row {
                    comment(
                        "When a Zserio compiler is configured, it checks the open schema and reports its errors and " +
                            "warnings in the editor (the same checks as running <code>java -jar zserio.jar</code>)."
                    )
                }
                row("Compiler (zserio.jar):") {
                    textFieldWithBrowseButton(
                        FileChooserDescriptorFactory.singleFile().withExtensionFilter("jar").withTitle("Select zserio.jar")
                    ).bindText(state::compilerJar).align(AlignX.FILL)
                }
                row("Java executable:") {
                    textFieldWithBrowseButton(
                        FileChooserDescriptorFactory.singleFile().withTitle("Select Java Executable")
                    ).bindText(state::javaExecutable).align(AlignX.FILL)
                        .comment("Leave empty to use the runtime of the IDE.")
                }
                row("Additional arguments:") {
                    textField().bindText(state::extraArguments).align(AlignX.FILL)
                        .comment("For example <code>-withoutWarnings unused</code>.")
                }
            }
        }
    }
}
