package com.asdev.edu.models

import android.content.Context
import com.asdev.edu.commitDiskDuser
import com.asdev.edu.readDiskDuser

object SharedData {

    private var duser: DUser? = null
    private var isDuserNull = false

    private fun init(appContext: Context) {
        duser = readDiskDuser(appContext)
        isDuserNull = duser == null
    }

    /**
     * Returns the DUser object as a read-only object. Changes will
     * not be committed to the disk. Any changes made to the returned
     * object will be discarded.
     */
    fun duserRo(appContext: Context): DUser? {
        if(isDuserNull)
            return null
        if(duser != null)
            return duser?.copy()

        // reload cuz duser got destroyed somehow
        init(appContext)
        return duser?.copy()
    }

    /**
     * Gets the DUser as a read-only object that will be passed in as
     * a parameter to the given block of code. Any changes made to the
     * provided object will be discarded.
     */
    fun duserRo(appContext: Context, block: (DUser?) -> Unit) {
        block(duserRo(appContext))
    }

    /**
     * Takes in a block of code with the param of the DUser object.
     * After the block is executed, the changes to the object
     * will be committed to the disk. THIS WILL NOT UPDATE
     * ON THE SERVER SIDE.
     */
    fun duserRw(appContext: Context, action: (DUser?) -> Unit) {
        if(!isDuserNull && duser == null) {
            init(appContext) // reload duser from disk
        }

        action(duser)

        duser?.let {
            commitDiskDuser(it, appContext)
        }
    }
}