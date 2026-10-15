import { readdirSync } from "node:fs";
import { fileURLToPath } from "node:url";
import { expect, test } from "vitest";
import { NodeCompiler } from "@myriaddreamin/typst-ts-node-compiler";

const workspace = fileURLToPath(new URL("../", import.meta.url));
const tests = readdirSync(new URL("./", import.meta.url)).filter((name) => name.endsWith(".test.typ")).sort();

expect(tests.length, "No Typst tests found").toBeGreaterThan(0);

for (const name of tests) {
    test(name, () => {
        const compiler = NodeCompiler.create({ workspace });
        const result = compiler.compile({ mainFilePath: fileURLToPath(new URL(name, import.meta.url)) });

        if (result.hasError()) {
            result.printErrors();
            expect.fail(`Typst assertions failed in ${name}`);
        }
        expect(result.result, `No compiled document for ${name}`).not.toBeNull();
    });
}

test("failed Typst assertions are reported as errors", () => {
    const compiler = NodeCompiler.create({ workspace });
    const result = compiler.compile({ mainFileContent: "#assert.eq(1, 2)" });

    expect(result.hasError()).toBe(true);
    expect(result.result).toBeNull();
});
