package org.zserio.intellij.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service

/** Application level settings of the optional integration of the Zserio compiler. */
@Service(Service.Level.APP)
@State(name = "ZserioSettings", storages = [Storage("zserio.xml")])
class ZserioSettings : PersistentStateComponent<ZserioSettings.State> {
    class State {
        /** Path to `zserio.jar`; the compiler is not used when empty. */
        var compilerJar: String = ""

        /** Java executable used to run the compiler; the runtime of the IDE is used when empty. */
        var javaExecutable: String = ""

        /** Additional command line arguments for the compiler, e.g. `-withoutWarnings unused`. */
        var extraArguments: String = ""
    }

    private var state = State()

    override fun getState(): State = state

    override fun loadState(state: State) {
        this.state = state
    }

    companion object {
        fun getInstance(): ZserioSettings = service()
    }
}
