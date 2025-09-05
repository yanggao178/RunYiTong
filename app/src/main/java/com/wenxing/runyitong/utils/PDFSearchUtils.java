package com.wenxing.runyitong.utils;

import android.graphics.pdf.PdfRenderer;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PDFSearchUtils {
    private static final String TAG = "PDFSearchUtils";
    
    public static class SearchResult {
        private int pageNumber;
        private String matchedText;
        private int startIndex;
        private int endIndex;
        private String contextBefore;
        private String contextAfter;
        
        public SearchResult(int pageNumber, String matchedText, int startIndex, int endIndex, 
                          String contextBefore, String contextAfter) {
            this.pageNumber = pageNumber;
            this.matchedText = matchedText;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.contextBefore = contextBefore;
            this.contextAfter = contextAfter;
        }
        
        // Getters
        public int getPageNumber() { return pageNumber; }
        public int getPageIndex() { return pageNumber; } // Alias for getPageNumber
        public String getMatchedText() { return matchedText; }
        public int getStartIndex() { return startIndex; }
        public int getEndIndex() { return endIndex; }
        public String getContextBefore() { return contextBefore; }
        public String getContextAfter() { return contextAfter; }
        
        public String getDisplayText() {
            return "第 " + (pageNumber + 1) + " 页: ..." + contextBefore + 
                   "[" + matchedText + "]" + contextAfter + "...";
        }
        
        @Override
        public String toString() {
            return "SearchResult{" +
                    "pageNumber=" + pageNumber +
                    ", matchedText='" + matchedText + '\'' +
                    ", startIndex=" + startIndex +
                    ", endIndex=" + endIndex +
                    '}';
        }
    }
    
    public static class SearchOptions {
        private boolean caseSensitive = false;
        private boolean wholeWord = false;
        private int maxResults = 50;
        private int contextLength = 20;
        
        public SearchOptions() {}
        
        public SearchOptions(boolean caseSensitive, boolean wholeWord) {
            this.caseSensitive = caseSensitive;
            this.wholeWord = wholeWord;
        }
        
        // Getters and Setters
        public boolean isCaseSensitive() { return caseSensitive; }
        public void setCaseSensitive(boolean caseSensitive) { this.caseSensitive = caseSensitive; }
        
        public boolean isWholeWord() { return wholeWord; }
        public void setWholeWord(boolean wholeWord) { this.wholeWord = wholeWord; }
        
        public int getMaxResults() { return maxResults; }
        public void setMaxResults(int maxResults) { this.maxResults = maxResults; }
        
        public int getContextLength() { return contextLength; }
        public void setContextLength(int contextLength) { this.contextLength = contextLength; }
    }
    
    /**
     * 在PDF中搜索文本（简化版本）
     */
    public static List<SearchResult> searchInPDF(PdfRenderer pdfRenderer, String query, boolean caseSensitive, boolean wholeWord) {
        List<SearchResult> results = new ArrayList<>();
        
        if (pdfRenderer == null || query == null || query.trim().isEmpty()) {
            return results;
        }
        
        try {
            int pageCount = pdfRenderer.getPageCount();
            
            for (int i = 0; i < pageCount; i++) {
                // 模拟搜索结果（实际应用中需要真正的PDF文本提取）
                if (mockSearchInPage(i, query, caseSensitive)) {
                    String mockText = generateMockPageText(i);
                    int startIndex = caseSensitive ? mockText.indexOf(query) : mockText.toLowerCase().indexOf(query.toLowerCase());
                    if (startIndex >= 0) {
                        String contextBefore = getContext(mockText, startIndex, 20, true);
                        String contextAfter = getContext(mockText, startIndex + query.length(), 20, false);
                        
                        SearchResult result = new SearchResult(
                            i,
                            query,
                            startIndex,
                            startIndex + query.length(),
                            contextBefore,
                            contextAfter
                        );
                        results.add(result);
                    }
                }
                
                // 限制结果数量
                if (results.size() >= 50) {
                    break;
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "搜索过程中发生错误", e);
        }
        
        return results;
    }
    
    /**
     * 在PDF中搜索文本（完整版本）
     * 注意：由于Android PdfRenderer API限制，此方法主要用于演示
     * 实际的PDF文本提取需要使用第三方库如PDFBox或iText
     */
    public static List<SearchResult> searchInPDF(PdfRenderer pdfRenderer, String query, SearchOptions options) {
        List<SearchResult> results = new ArrayList<>();
        
        if (pdfRenderer == null || query == null || query.trim().isEmpty()) {
            return results;
        }
        
        String cleanQuery = cleanSearchQuery(query);
        if (cleanQuery.isEmpty()) {
            return results;
        }
        
        try {
            int pageCount = pdfRenderer.getPageCount();
            Log.d(TAG, "开始搜索，共 " + pageCount + " 页，关键词: " + cleanQuery);
            
            for (int i = 0; i < pageCount && results.size() < options.getMaxResults(); i++) {
                List<SearchResult> pageResults = searchInPage(i, cleanQuery, options);
                results.addAll(pageResults);
                
                if (results.size() >= options.getMaxResults()) {
                    results = results.subList(0, options.getMaxResults());
                    break;
                }
            }
            
            Log.d(TAG, "搜索完成，找到 " + results.size() + " 个结果");
            
        } catch (Exception e) {
            Log.e(TAG, "搜索过程中发生错误", e);
        }
        
        return results;
    }
    
    /**
     * 模拟页面搜索（实际应用中需要真正的PDF文本提取）
     */
    private static boolean mockSearchInPage(int pageIndex, String query, boolean caseSensitive) {
        // 模拟一些页面包含搜索内容
        String mockContent = generateMockPageText(pageIndex);
        
        if (caseSensitive) {
            return mockContent.contains(query);
        } else {
            return mockContent.toLowerCase().contains(query.toLowerCase());
        }
    }
    
    /**
     * 在单个页面中搜索文本
     * 注意：这是一个模拟实现，实际需要PDF文本提取功能
     */
    private static List<SearchResult> searchInPage(int pageNumber, String query, SearchOptions options) {
        List<SearchResult> results = new ArrayList<>();
        
        try {
            // 模拟页面文本内容（实际应该从PDF页面提取文本）
            String pageText = generateMockPageText(pageNumber);
            
            if (pageText.isEmpty()) {
                return results;
            }
            
            // 构建搜索模式
            String searchPattern = buildSearchPattern(query, options);
            int flags = options.isCaseSensitive() ? 0 : Pattern.CASE_INSENSITIVE;
            Pattern pattern = Pattern.compile(searchPattern, flags);
            
            Matcher matcher = pattern.matcher(pageText);
            
            while (matcher.find() && results.size() < options.getMaxResults()) {
                String matchedText = matcher.group();
                int startIndex = matcher.start();
                int endIndex = matcher.end();
                
                // 获取上下文
                String contextBefore = getContext(pageText, startIndex, options.getContextLength(), true);
                String contextAfter = getContext(pageText, endIndex, options.getContextLength(), false);
                
                SearchResult result = new SearchResult(
                    pageNumber, matchedText, startIndex, endIndex, contextBefore, contextAfter
                );
                
                results.add(result);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "在页面 " + (pageNumber + 1) + " 搜索时发生错误", e);
        }
        
        return results;
    }
    
    /**
     * 构建搜索模式
     */
    private static String buildSearchPattern(String query, SearchOptions options) {
        String escapedQuery = Pattern.quote(query);
        
        if (options.isWholeWord()) {
            return "\\b" + escapedQuery + "\\b";
        } else {
            return escapedQuery;
        }
    }
    
    /**
     * 获取匹配文本的上下文
     */
    private static String getContext(String text, int position, int contextLength, boolean before) {
        if (before) {
            int start = Math.max(0, position - contextLength);
            return text.substring(start, position);
        } else {
            int end = Math.min(text.length(), position + contextLength);
            return text.substring(position, end);
        }
    }
    
    /**
     * 生成模拟页面文本（用于演示）
     * 实际应用中应该使用PDF文本提取库
     */
    private static String generateMockPageText(int pageNumber) {
        // 模拟不同页面的文本内容
        String[] mockTexts = {
            "这是第一页的内容。包含医学相关的文本信息，用于演示搜索功能。医学研究表明，定期检查对健康很重要。",
            "第二页讨论了医疗技术的发展。人工智能在医学诊断中的应用越来越广泛，提高了诊断的准确性。",
            "第三页介绍了常见疾病的预防方法。预防胜于治疗，保持良好的生活习惯是关键。",
            "第四页分析了医疗数据的重要性。大数据分析帮助医生做出更好的治疗决策。",
            "第五页探讨了远程医疗的优势。特别是在偏远地区，远程医疗提供了重要的医疗服务。"
        };
        
        if (pageNumber < mockTexts.length) {
            return mockTexts[pageNumber];
        } else {
            return "第 " + (pageNumber + 1) + " 页的内容。这里包含了医学文档的相关信息，用于搜索功能的演示。";
        }
    }
    
    /**
     * 高亮显示搜索结果
     */
    public static String highlightSearchResults(String text, String query, SearchOptions options) {
        if (text == null || query == null || query.trim().isEmpty()) {
            return text;
        }
        
        try {
            String searchPattern = buildSearchPattern(query, options);
            int flags = options.isCaseSensitive() ? 0 : Pattern.CASE_INSENSITIVE;
            Pattern pattern = Pattern.compile(searchPattern, flags);
            
            return pattern.matcher(text).replaceAll("<mark>$0</mark>");
            
        } catch (Exception e) {
            Log.e(TAG, "高亮文本时发生错误", e);
            return text;
        }
    }
    
    /**
     * 验证搜索查询
     */
    public static boolean isValidSearchQuery(String query) {
        return query != null && !query.trim().isEmpty() && query.trim().length() >= 1;
    }
    
    /**
     * 清理搜索查询
     */
    public static String cleanSearchQuery(String query) {
        if (query == null) {
            return "";
        }
        
        return query.trim().replaceAll("\\s+", " ");
    }
    
    /**
     * 获取搜索统计信息
     */
    public static String getSearchStatistics(List<SearchResult> results, String query) {
        if (results.isEmpty()) {
            return "未找到包含 \"" + query + "\" 的内容";
        }
        
        int totalMatches = results.size();
        int pageCount = (int) results.stream().mapToInt(SearchResult::getPageNumber).distinct().count();
        
        return "找到 " + totalMatches + " 个匹配项，分布在 " + pageCount + " 页中";
    }
}