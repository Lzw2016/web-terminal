package org.pty4j.web.websocket.mock.git;

import lombok.Data;

/**
 * 代码branch或Tag
 * <p>
 * 作者： lzw<br/>
 * 创建时间：2018-01-31 10:41 <br/>
 */

@Data
public class GitBranch {
    /**
     * 代码提交ID(commitID)
     */
    private String commitId;

    /**
     * 代码branch或Tag
     */
    private String branch;

    public GitBranch() {
    }

    /**
     * @param commitId 代码提交ID(commitID)
     * @param branch   代码branch或Tag
     */
    public GitBranch(String commitId, String branch) {
        this.commitId = commitId;
        this.branch = branch;
    }
}