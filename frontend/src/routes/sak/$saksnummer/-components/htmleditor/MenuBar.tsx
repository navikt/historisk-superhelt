import { ArrowRedoIcon, ArrowUndoIcon, BulletListIcon, NumberListIcon, PencilIcon } from "@navikt/aksel-icons";
import { ActionMenu, BodyShort, Box, Button, Detail, HelpText, HStack, Tooltip, VStack } from "@navikt/ds-react";
import { type Editor, useEditorState } from "@tiptap/react";
import { Bold, Italic } from "./Icons";
import styles from "./MenuBar.module.css";

export function MenuBar({ editor }: { editor: Editor }) {
    // Read the current editor's state, and re-render the component when it changes
    const editorState = useEditorState({
        editor,
        selector: (ctx) => {
            return {
                isBold: ctx.editor.isActive("bold") ?? false,
                canBold: ctx.editor.can().chain().toggleBold().run() ?? false,
                isItalic: ctx.editor.isActive("italic") ?? false,
                canItalic: ctx.editor.can().chain().toggleItalic().run() ?? false,
                isStrike: ctx.editor.isActive("strike") ?? false,
                canStrike: ctx.editor.can().chain().toggleStrike().run() ?? false,
                isCode: ctx.editor.isActive("code") ?? false,
                canCode: ctx.editor.can().chain().toggleCode().run() ?? false,
                canClearMarks: ctx.editor.can().chain().unsetAllMarks().run() ?? false,
                isParagraph: ctx.editor.isActive("paragraph") ?? false,
                activeHeading:
                    ([1, 2, 3, 4] as const).find((level) => ctx.editor.isActive("heading", { level })) ?? null,
                isBulletList: ctx.editor.isActive("bulletList") ?? false,
                isOrderedList: ctx.editor.isActive("orderedList") ?? false,
                isCodeBlock: ctx.editor.isActive("codeBlock") ?? false,
                isBlockquote: ctx.editor.isActive("blockquote") ?? false,
                isHighlight: ctx.editor.isActive("highlight") ?? false,
                canUndo: ctx.editor.can().chain().undo().run() ?? false,
                canRedo: ctx.editor.can().chain().redo().run() ?? false,
            };
        },
    });

    function headingLevelToSize(level: number) {
        switch (level) {
            case 1:
                return "16pt";
            case 2:
                return "13pt";
            case 3:
                return "12pt";
            case 4:
            case 5:
                return "11pt";
            case 6:
                return "10pt";
        }
    }

    return (
        <Box
            background="sunken"
            padding="space-4"
            borderRadius="8"
            borderWidth="1"
            borderColor="neutral-subtle"
            asChild
        >
            <HStack align="center" justify="space-between" paddingInline="space-4 space-8">
                <HStack className={styles.redigeringsMeny} gap="space-4" align="center" justify="start">
                    <Tooltip content="Angre" keys={["Ctrl", "Z"]} placement="top">
                        <Button
                            type="button"
                            variant="tertiary"
                            data-color="neutral"
                            size="small"
                            aria-label="Angre"
                            onClick={() => editor.chain().focus().undo().run()}
                            disabled={!editor.can().undo()}
                            icon={<ArrowUndoIcon fontSize="1.25rem" />}
                        />
                    </Tooltip>

                    <Tooltip content="Gjør om igjen" keys={["Ctrl", "Shift", "Z"]} placement="top">
                        <Button
                            type="button"
                            variant="tertiary"
                            data-color="neutral"
                            size="small"
                            aria-label="Gjør om igjen"
                            onClick={() => editor.chain().focus().redo().run()}
                            disabled={!editor.can().redo()}
                            icon={<ArrowRedoIcon fontSize="1.25rem" />}
                        />
                    </Tooltip>

                    <Tooltip content="Fet" keys={["Ctrl", "B"]} placement="top">
                        <Button
                            type="button"
                            variant="tertiary"
                            data-color="neutral"
                            size="small"
                            aria-label="Fet"
                            onClick={() => editor.chain().focus().toggleBold().run()}
                            className={`${editorState.isBold && styles.active}`}
                            disabled={!editorState.canBold}
                            icon={<Bold />}
                        />
                    </Tooltip>

                    <Tooltip content="Kursiv" keys={["Ctrl", "I"]} placement="top">
                        <Button
                            type="button"
                            variant="tertiary"
                            data-color="neutral"
                            size="small"
                            aria-label="Kursiv"
                            onClick={() => editor.chain().focus().toggleItalic().run()}
                            className={`${editorState.isItalic && styles.active}`}
                            disabled={!editorState.canItalic}
                            icon={<Italic />}
                        />
                    </Tooltip>

                    <ActionMenu>
                        <ActionMenu.Trigger>
                            <Button
                                type="button"
                                variant="tertiary"
                                data-color="neutral"
                                size="small"
                                aria-label="Overskrift"
                                style={{ width: "2rem" }}
                                className={`${editorState.activeHeading && styles.active}`}
                            >
                                {editorState.activeHeading ? `H${editorState.activeHeading}` : "H"}
                            </Button>
                        </ActionMenu.Trigger>
                        <ActionMenu.Content>
                            {([1, 2, 3, 4] as const).map((level) => (
                                <ActionMenu.Item
                                    key={level}
                                    shortcut={`Ctrl + Alt + ${level}`}
                                    onSelect={() => editor.chain().focus().toggleHeading({ level }).run()}
                                    style={{ cursor: "pointer" }}
                                >
                                    <span style={{ fontSize: headingLevelToSize(level), fontWeight: 500 }}>
                                        Overskrift {level}
                                    </span>
                                </ActionMenu.Item>
                            ))}
                        </ActionMenu.Content>
                    </ActionMenu>

                    <Tooltip content="Nummerert liste" keys={["Ctrl", "Shift", "7"]} placement="top">
                        <Button
                            type="button"
                            variant="tertiary"
                            data-color="neutral"
                            size="small"
                            aria-label="Nummerert liste"
                            onClick={() => editor.chain().focus().toggleOrderedList().run()}
                            className={editorState.isOrderedList ? styles.active : ""}
                            icon={<NumberListIcon fontSize="1.25rem" />}
                        />
                    </Tooltip>

                    <Tooltip content="Punktliste" keys={["Ctrl", "Shift", "8"]} placement="top">
                        <Button
                            type="button"
                            variant="tertiary"
                            data-color="neutral"
                            size="small"
                            aria-label="Punktliste"
                            onClick={() => editor.chain().focus().toggleBulletList().run()}
                            className={editorState.isBulletList ? styles.active : ""}
                            icon={<BulletListIcon fontSize="1.25rem" />}
                        />
                    </Tooltip>

                    <Tooltip content="Marker tekst" placement="top">
                        <Button
                            type="button"
                            variant="tertiary"
                            data-color="neutral"
                            size="small"
                            aria-label="Marker tekst"
                            onClick={() => editor.chain().focus().toggleHighlight().run()}
                            className={editorState.isHighlight ? styles.active : ""}
                            icon={
                                <span style={{ position: "relative", display: "inline-flex" }}>
                                    <PencilIcon fontSize="1.25rem" />
                                    <span
                                        style={{
                                            position: "absolute",
                                            bottom: 0,
                                            right: 0,
                                            width: "7px",
                                            height: "7px",
                                            backgroundColor: "var(--ax-bg-warning-moderate-hoverA)",
                                            border: "1px solid var(--ax-bg-warning-strong)",
                                            borderRadius: "100%",
                                        }}
                                    />
                                </span>
                            }
                        />
                    </Tooltip>
                </HStack>
                <HelpText title="Hvordan redigere brev">
                    <VStack gap="space-6">
                        <BodyShort size="small">
                            Brev-editoren fungerer stort sett likt som Word, og deler mange av de samme hurtigtastene.
                        </BodyShort>
                        <BodyShort size="small">
                            Hold musepekeren over knappene i redigeringsmenyen for å se hvilke hurtigtaster som finnes
                            for de ulike funksjonene.
                        </BodyShort>
                        <BodyShort size="small">
                            For å lage en ny linje, trykk <Detail as="kbd">Shift</Detail>
                            {" + "}
                            <Detail as="kbd">Enter</Detail>. For å lage et nytt avsnitt, trykk{" "}
                            <Detail as="kbd">Enter</Detail>.
                        </BodyShort>
                        <BodyShort size="small">
                            Slik innholdet ser ut i redigeringsverktøyet er også slik det vil se ut i det ferdige
                            brevet.
                        </BodyShort>
                    </VStack>
                </HelpText>
            </HStack>
        </Box>
    );
}
