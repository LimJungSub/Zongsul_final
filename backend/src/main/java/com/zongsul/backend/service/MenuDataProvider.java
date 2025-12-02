package com.zongsul.backend.service;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * MenuDataProvider
 * - 메뉴 추천에 사용할 음식 데이터 풀을 관리하는 싱글톤 컴포넌트
 * - 5가지 카테고리별 메뉴 리스트 제공
 */
@Component
public class MenuDataProvider {

    // 1. 밥 종류 (5개)
    private final List<String> riceList = List.of(
            "흰쌀밥", "잡곡밥", "흑미밥", "차조밥", "콩나물밥"
    );

    // 2. 국 종류 (15개)
    private final List<String> soupList = List.of(
            "곰탕", "미역국", "소고기무국", "된장국", "김치찌개",
            "순두부찌개", "육개장", "콩나물국", "북엇국", "어묵국",
            "달걀국", "시금치된장국", "떡만둣국", "감자양파국", "갈비탕"
    );

    // 3. 메인 반찬 (고기/해물) (20개)
    private final List<String> mainDishList = List.of(
            "불고기", "제육볶음", "간장닭조림(찜닭)", "고등어구이", "돈까스",
            "닭갈비", "오징어볶음", "탕수육", "떡갈비", "함박스테이크",
            "가자미구이", "순살치킨", "동그랑땡", "소갈비찜", "삼치구이",
            "코다리조림", "두부김치", "보쌈", "낙지볶음", "훈제오리"
    );

    // 4. 서브 메뉴 (15개)
    private final List<String> subMenuList = List.of(
            "계란찜", "김치전", "김자반", "잡채", "메추리알장조림",
            "국물떡볶이", "멸치볶음", "어묵볶음", "소세지야채볶음", "감자조림",
            "두부조림", "맛살튀김", "군만두", "도토리묵무침", "쫄면"
    );

    // 5. 나물류 (10개)
    private final List<String> namulList = List.of(
            "시금치무침", "콩나물무침", "오이무침", "무생채", "숙주나물",
            "고사리나물", "미역줄기볶음", "가지나물", "도라지무침", "애호박볶음"
    );

    public List<String> getRiceList() { return riceList; }
    public List<String> getSoupList() { return soupList; }
    public List<String> getMainDishList() { return mainDishList; }
    public List<String> getSubMenuList() { return subMenuList; }
    public List<String> getNamulList() { return namulList; }
}
