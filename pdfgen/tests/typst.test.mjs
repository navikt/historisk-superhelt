import assert from "node:assert/strict";
import { readdirSync } from "node:fs";
import { fileURLToPath } from "node:url";
import test from "node:test";
import { NodeCompiler } from "@myriaddreamin/typst-ts-node-compiler";

const workspace = fileURLToPath(new URL("../", import.meta.url));
const tests = readdirSync(new URL("./", import.meta.url)).filter((name) => name.endsWith(".typ")).sort();

assert.ok(tests.length > 0, "No Typst tests found");

for (const name of tests) {
    test(name, () => {
        const compiler = NodeCompiler.create({ workspace });
        const result = compiler.compile({ mainFilePath: fileURLToPath(new URL(name, import.meta.url)) });

        if (result.hasError()) {
            result.printErrors();
            assert.fail(`Typst assertions failed in ${name}`);
        }
        assert.ok(result.result, `No compiled document for ${name}`);
    });
}

test("failed Typst assertions are reported as errors", () => {
    const compiler = NodeCompiler.create({ workspace });
    const result = compiler.compile({ mainFileContent: "#assert.eq(1, 2)" });

    assert.equal(result.hasError(), true);
    assert.equal(result.result, null);
});
