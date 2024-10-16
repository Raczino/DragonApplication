package com.raczkowski.app.hashtags;

import com.raczkowski.app.article.Article;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Hashtag {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false, unique = true)
    private String tag;

    @ManyToMany(mappedBy = "hashtags")
    private List<Article> articles;

    public Hashtag(String tag) {
        this.tag = tag;
    }
}
