package com.shonkware.droidmodloader.engine.install

internal object InstallerOptionSelectionHelper {

    fun toggleOption(
        groups: List<InstallerGroup>,
        selectedOptionIds: Set<String>,
        optionId: String
    ): Set<String> {
        val group = groups.firstOrNull { installerGroup ->
            installerGroup.options.any { option -> option.id == optionId }
        } ?: return selectedOptionIds

        val option = group.options.firstOrNull { it.id == optionId }
            ?: return selectedOptionIds

        if (option.required) {
            return selectedOptionIds + optionId
        }

        val groupOptionIds = group.options.map { it.id }.toSet()
        val selected = selectedOptionIds.toMutableSet()
        val isSelected = optionId in selected

        return when (group.type) {
            InstallerGroupType.SELECT_EXACTLY_ONE -> {
                selected.removeAll(groupOptionIds)
                selected.add(optionId)
                selected
            }

            InstallerGroupType.SELECT_AT_MOST_ONE -> {
                selected.removeAll(groupOptionIds)

                if (!isSelected) {
                    selected.add(optionId)
                }

                selected
            }

            InstallerGroupType.SELECT_AT_LEAST_ONE -> {
                if (isSelected) {
                    val selectedInGroup = selected.count { it in groupOptionIds }

                    if (selectedInGroup <= 1) {
                        selected
                    } else {
                        selected.remove(optionId)
                        selected
                    }
                } else {
                    selected.add(optionId)
                    selected
                }
            }

            InstallerGroupType.SELECT_ANY -> {
                if (isSelected) {
                    selected.remove(optionId)
                } else {
                    selected.add(optionId)
                }

                selected
            }
        }.toSet()
    }
}