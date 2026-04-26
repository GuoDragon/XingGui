package com.example.xinggui.data.repository

import com.example.xinggui.data.model.ResourceItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class ResourceAssetsConsistencyTest {
    @Test
    fun resourceJsonAssetPathsPointToExistingFiles() {
        val assetsDir = assetsDir()
        val resources = readResources(assetsDir)

        resources.forEach { item ->
            val assetPath = item.assetPath
            assertTrue("${item.resourceId} 缺少 assetPath", !assetPath.isNullOrBlank())
            assertTrue("${item.resourceId} 文件不存在：$assetPath", Files.exists(assetsDir.resolve(assetPath!!)))
        }
    }

    @Test
    fun nonCdcResourceMetadataSourceAndFilenamesStayOnSameTopic() {
        val assetsDir = assetsDir()
        val resourcesById = readResources(assetsDir).associateBy { it.resourceId }

        NonCdcResourceExpectations.forEach { (resourceId, expectation) ->
            val item = resourcesById[resourceId] ?: error("$resourceId 缺少资源数据")
            val assetPath = item.assetPath.orEmpty()
            val sourcePath = sourcePathFor(assetsDir, item)
            val sourceText = String(Files.readAllBytes(sourcePath), Charsets.UTF_8)

            assertTrue("$resourceId 文件名与主题不一致：$assetPath", assetPath.endsWith(expectation.fileName))
            expectation.titleKeywords.forEach { keyword ->
                assertTrue("$resourceId 标题缺少关键词：$keyword", item.title.contains(keyword))
            }
            assertTrue("$resourceId 源内容标题不一致", sourceText.startsWith("# ${item.title}"))
            expectation.sourceKeywords.forEach { keyword ->
                assertTrue("$resourceId 源内容缺少主题关键词：$keyword", sourceText.contains(keyword))
            }
            ForbiddenCdcTextMarkers.forEach { forbidden ->
                assertFalse("$resourceId 源内容不应包含 CDC 模板内容：$forbidden", sourceText.contains(forbidden, ignoreCase = true))
            }
            assertFalse("$resourceId 源内容不应保留英文占位说明", sourceText.contains("XingGui original education resource", ignoreCase = true))
        }
    }

    @Test
    fun nonCdcPdfExtractedTextMatchesResourceTitlesWhenExtractorAvailable() {
        val extractor = findExecutableOnPath("pdftotext") ?: return
        val assetsDir = assetsDir()
        val resourcesById = readResources(assetsDir).associateBy { it.resourceId }

        NonCdcResourceExpectations.forEach { (resourceId, expectation) ->
            val item = resourcesById[resourceId] ?: error("$resourceId 缺少资源数据")
            val pdf = assetsDir.resolve(item.assetPath.orEmpty())
            val process = ProcessBuilder(extractor.toString(), "-enc", "UTF-8", pdf.toString(), "-")
                .redirectErrorStream(true)
                .start()
            val text = process.inputStream.bufferedReader(Charsets.UTF_8).readText()
            val exitCode = process.waitFor()

            assertEquals("$resourceId PDF 文本提取失败：$text", 0, exitCode)
            assertTrue("$resourceId PDF 标题与 resources.json 不一致", text.contains(item.title))
            expectation.sourceKeywords.take(2).forEach { keyword ->
                assertTrue("$resourceId PDF 正文缺少主题关键词：$keyword", text.contains(keyword))
            }
            ForbiddenCdcTextMarkers.forEach { forbidden ->
                assertFalse("$resourceId PDF 不应显示 CDC 里程碑内容：$forbidden", text.contains(forbidden, ignoreCase = true))
            }
        }
    }

    @Test
    fun nonCdcResourcesDoNotPointToCdcFilesOrDuplicateCdcPdfBytes() {
        val assetsDir = assetsDir()
        val resources = readResources(assetsDir)
        val cdcFiles = resources
            .filter { it.resourceId.startsWith("res1") }
            .mapNotNull { it.assetPath }
            .map { assetsDir.resolve(it) }

        resources.filter { it.resourceId in "res001".."res012" }.forEach { item ->
            val assetPath = item.assetPath.orEmpty()
            assertFalse("${item.resourceId} 不应指向 CDC 文件", assetPath.contains("_cdc_", ignoreCase = true))
            val file = assetsDir.resolve(assetPath)
            val fileBytes = Files.readAllBytes(file)
            val duplicatesCdc = cdcFiles.any { cdcFile -> fileBytes.contentEquals(Files.readAllBytes(cdcFile)) }
            assertFalse("${item.resourceId} PDF 内容不应复用 CDC 文件", duplicatesCdc)
        }
    }

    private fun readResources(assetsDir: Path): List<ResourceItem> {
        val json = String(Files.readAllBytes(assetsDir.resolve("data/resources.json")), Charsets.UTF_8)
        val type = object : TypeToken<List<ResourceItem>>() {}.type
        return Gson().fromJson(json, type)
    }

    private fun sourcePathFor(assetsDir: Path, item: ResourceItem): Path {
        val assetPath = item.assetPath.orEmpty()
        val pdfStem = Paths.get(assetPath).fileName.toString().removeSuffix(".pdf")
        return assetsDir.resolve("resources/pdf_sources/${item.resourceId}_$pdfStem.md")
    }

    private fun findExecutableOnPath(command: String): Path? {
        val executableNames = if (System.getProperty("os.name").contains("Windows", ignoreCase = true)) {
            listOf("$command.exe", command)
        } else {
            listOf(command)
        }
        return System.getenv("PATH").orEmpty()
            .split(File.pathSeparator)
            .asSequence()
            .filter { it.isNotBlank() }
            .flatMap { directory -> executableNames.asSequence().map { Paths.get(directory, it) } }
            .firstOrNull { Files.isRegularFile(it) && Files.isExecutable(it) }
    }

    private fun assetsDir(): Path {
        return listOf(
            Paths.get("src/main/assets"),
            Paths.get("app/src/main/assets")
        ).map { it.toAbsolutePath().normalize() }
            .first { Files.exists(it.resolve("data/resources.json")) }
    }

    private data class ResourceExpectation(
        val fileName: String,
        val titleKeywords: List<String>,
        val sourceKeywords: List<String>
    )

    private companion object {
        val ForbiddenCdcTextMarkers = listOf(
            "Your baby at 2 months",
            "Milestone Moments",
            "CDC",
            "Centers for Disease"
        )

        val NonCdcResourceExpectations = mapOf(
            "res001" to ResourceExpectation(
                fileName = "res001_inclusive_education_guide.pdf",
                titleKeywords = listOf("融合教育政策要点解读", "融合教育"),
                sourceKeywords = listOf("融合教育", "政策落地", "家校")
            ),
            "res002" to ResourceExpectation(
                fileName = "res002_iep_writing_guide.pdf",
                titleKeywords = listOf("IEP"),
                sourceKeywords = listOf("IEP", "可测量目标", "进度记录")
            ),
            "res003" to ResourceExpectation(
                fileName = "res003_autism_support_strategies.pdf",
                titleKeywords = listOf("自闭症"),
                sourceKeywords = listOf("自闭症", "视觉日程", "沟通支持")
            ),
            "res004" to ResourceExpectation(
                fileName = "res004_sensory_integration_activities.pdf",
                titleKeywords = listOf("感觉统合"),
                sourceKeywords = listOf("感觉统合", "本体觉", "触觉")
            ),
            "res005" to ResourceExpectation(
                fileName = "res005_language_development.pdf",
                titleKeywords = listOf("语言发展"),
                sourceKeywords = listOf("语言发展", "家庭情境", "学校情境")
            ),
            "res006" to ResourceExpectation(
                fileName = "res006_adhd_classroom_management.pdf",
                titleKeywords = listOf("ADHD"),
                sourceKeywords = listOf("ADHD", "课堂环境", "行为支持")
            ),
            "res007" to ResourceExpectation(
                fileName = "res007_visual_supports.pdf",
                titleKeywords = listOf("视觉支持"),
                sourceKeywords = listOf("视觉支持", "视觉日程", "选择板")
            ),
            "res008" to ResourceExpectation(
                fileName = "res008_parent_training.pdf",
                titleKeywords = listOf("早期干预"),
                sourceKeywords = listOf("早期干预", "家庭练习", "照护者")
            ),
            "res009" to ResourceExpectation(
                fileName = "res009_social_skills_training.pdf",
                titleKeywords = listOf("社交技能"),
                sourceKeywords = listOf("社交技能", "角色扮演", "泛化")
            ),
            "res010" to ResourceExpectation(
                fileName = "res010_aac_guide.pdf",
                titleKeywords = listOf("AAC"),
                sourceKeywords = listOf("AAC", "辅助沟通系统", "沟通板")
            ),
            "res011" to ResourceExpectation(
                fileName = "res011_assessment_tools.pdf",
                titleKeywords = listOf("评估工具"),
                sourceKeywords = listOf("特殊教育评估", "多元资料", "结果转化")
            ),
            "res012" to ResourceExpectation(
                fileName = "res012_inclusive_teaching.pdf",
                titleKeywords = listOf("融合课堂"),
                sourceKeywords = listOf("融合课堂", "差异化教学", "同伴支持")
            )
        )
    }
}
