package com.example.siteback.DTO;

import com.example.siteback.Entity.BaUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaUserDTO {
    private String id;
    private String userId;
    private String username;
    private String email;
    private String phoneNumber;
    private String bro ;
    private List<String> friendList ;
    private List<String> articleList ;
    private int level;
    private int coins;
    private String url;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public BaUserDTO(BaUser baUser){
        this.id = baUser.getId().toString();
        this.userId = baUser.getUserId();
        this.username = baUser.getUsername();
        this.email = baUser.getEmail();
        this.phoneNumber =baUser.getPhoneNumber();
        this.bro = baUser.getBro();
        this.friendList = baUser.getFriendList();
        this.articleList = baUser.getArticleList();
        this.level = baUser.getLevel();
        this.coins = baUser.getCoins();
        this.url = baUser.getUrl();
        this.createdAt = baUser.getCreatedAt();
        this.updatedAt = baUser.getUpdatedAt();

    }
}


/*DTO 层: DTO 的主要目的是用于数据传输，通常只需要传输业务上需要的数据
*        分离关注点：
DTO 层使得数据传输的结构与内部模型（如数据库实体）解耦。这样，前端和后端的请求/响应格式可以独立于数据库模型进行设计，
从而提高系统的灵活性。
        提高可维护性：
随着系统的发展，数据库模型（如 BaUser）可能会发生变化（如字段的增加或修改）。如果没有 DTO 层，这些变化可能直接影响到控制器或客户端，
而通过 DTO 层，前端和控制器可以根据新的需求修改 DTO，而不必直接修改实体模型。
        数据安全：
DTO 层可以控制暴露给客户端的数据。对于敏感字段（如密码或安全问题答案），可以避免将其直接暴露给客户端。例如，
返回给前端的用户信息不必包含密码字段，而是包含一个 token。
        数据格式化：
DTO 可以用于对数据进行格式化，例如将时间戳转换为更易读的日期格式，或者将多个字段合并为一个更简洁的字段。
这可以确保你返回给客户端的数据始终以一致的格式传输。
        API 版本控制：
DTO 层可以帮助处理 API 的版本控制。通过为不同的 API 版本设计不同的 DTO 结构，可以灵活地支持不同版本的客户端请求和响应。
        耦合性较高：
如果直接在控制器中使用数据库实体类，数据库模型的变化会直接影响到控制器层和前端。这样，当数据库模型发生变化时，
你可能需要修改多个地方，增加了维护的难度。
        数据暴露问题：
如果直接暴露数据库实体类，可能会无意中将敏感数据（如密码、盐值等）暴露给客户端。没有 DTO 层就难以控制哪些数据应该被暴露，
哪些不应该。
        缺乏灵活性：
随着系统的演进，前端可能需要的数据格式和后台数据格式不同，直接返回实体类会导致前端和后端之间的耦合较强，
修改一个字段格式可能会影响前端的展示逻辑。
        安全性和数据一致性问题：
如果不使用 DTO 层，你可能难以控制哪些字段对外可见，尤其是在涉及到安全数据（如密码）时，直接使用实体类可能会造成安全隐患。
* */