package kr.rendog.client.command.commands

import kr.rendog.client.command.ClientCommand
import kr.rendog.client.util.FolderUtils

object OpenFolderCommand : ClientCommand(
    name = "openfolder",
    alias = arrayOf("of", "open"),
    description = "Open any RendogClient folder"
) {
    init {
        literal("rendog") {
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