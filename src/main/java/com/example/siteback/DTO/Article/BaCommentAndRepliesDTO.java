package com.example.siteback.DTO.Article;

import com.example.siteback.Entity.BaComments;
import com.example.siteback.Entity.BaReplies;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BaCommentAndRepliesDTO {

    private List<BaComments> listComments;

    private List<BaReplies> listReplies;

}
