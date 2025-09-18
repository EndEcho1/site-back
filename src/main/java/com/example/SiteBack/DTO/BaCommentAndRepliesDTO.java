package com.example.SiteBack.DTO;

import com.example.SiteBack.Entity.BaComments;
import com.example.SiteBack.Entity.BaReplies;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BaCommentAndRepliesDTO {

    private List<BaComments> listComments;

    private List<BaReplies> listReplies;

}
