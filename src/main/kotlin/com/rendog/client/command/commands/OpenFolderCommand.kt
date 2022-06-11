package com.rendog.client.command.commands

import com.rendog.client.command.ClientCommand
import com.rendog.client.util.FolderUtils

object OpenFolderCommand : ClientCommand(
    name = "openfolder",
    alias = arrayOf("of", "open"),
    description = "Open any Lambda folder"
) {
    init {
        literal("lambda") {
            execute {
                FolderUtils.openFolder(FolderUtils.rendogFolder)
            }
        }

        literal("packetLogs") {
            execute {
                FolderUtils.openFolder(FolderUtils.packetLogFolder)
            }
        }

        literal("songs") {
            execute {
                FolderUtils.openFolder(FolderUtils.songFolder)
            }
        }

        literal("screenshots") {
            execute {
                FolderUtils.openFolder(FolderUtils.screenshotFolder)
            }
        }

        literal("logs") {
            execute {
                FolderUtils.openFolder(FolderUtils.logFolder)
            }
        }

        execute {
            FolderUtils.openFolder(FolderUtils.rendogFolder)
        }
    }
}