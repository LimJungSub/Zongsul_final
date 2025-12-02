import { useState, useEffect, useRef } from "react";
import "./App.css";

function App() {
  const fileInputRef = useRef(null);
  const captureRef = useRef(null);

  const [mode, setMode] = useState("guest");
  const [page, setPage] = useState("home");
  const [name, setName] = useState("");
  const [studentId, setStudentId] = useState("");
  const [selectedDay, setSelectedDay] = useState("");
  const [dishes, setDishes] = useState([
    { name: "", count: "" },
    { name: "", count: "" },
    { name: "", count: "" },
    { name: "", count: "" },
  ]);
  const [distributedDishes, setDistributedDishes] = useState([]);

  const [uploadedDays, setUploadedDays] = useState({
    월: true,
    화: true,
    수: true,
    목: true,
    금: false,
  });

  const [analysisComplete, setAnalysisComplete] = useState(false);
  const [fridayAnalysisResult, setFridayAnalysisResult] = useState(null);

  const [mockResults] = useState({
    월: {
      ratios: { 불고기: 0.12, 잡채: 0.28, 미역줄기볶음: 0.6 },
      leastPopular: "미역줄기볶음",
      related: ["고사리나물", "숙주무침"],
    },
    화: {
      ratios: { 고등어구이: 0.3, 김치전: 0.45, 콩나물무침: 0.25 },
      leastPopular: "김치전",
      related: ["군만두", "감자전"],
    },
    수: {
      ratios: { 오징어볶음: 0.2, 멸치볶음: 0.35, 도라지무침: 0.45 },
      leastPopular: "도라지무침",
      related: ["애호박볶음", "브로콜리"],
    },
    목: {
      ratios: { 돈까스: 0.15, 국물떡볶이: 0.35, 오이무침: 0.5 },
      leastPopular: "오이무침",
      related: ["두부무침", "무생채"],
    },
  });

  const menuDB = [
    { category: "밥", name: "흰쌀밥" },
    { category: "국", name: "된장국" },
    { category: "메인", name: "제육볶음" },
    { category: "메인", name: "고등어구이" },
    { category: "서브", name: "계란찜" },
    { category: "서브", name: "김자반" },
    { category: "서브", name: "시금치" },
    { category: "서브", name: "두부조림" },
    { category: "서브", name: "멸치볶음" },
    { category: "서브", name: "브로콜리" },
    { category: "서브", name: "어묵볶음" },
  ];

  const handleUpload = async () => {
    const files = fileInputRef.current.files;
    if (!files || files.length === 0)
      return alert("업로드할 사진을 선택하세요.");

    if (selectedDay !== "금")
      return alert("현재는 금요일만 업로드 가능합니다.");

    const form = new FormData();
    for (let i = 0; i < files.length; i++) form.append("images", files[i]);

    try {
      const res = await fetch(
        "http://zongsul-env.eba-xmxykbwh.ap-northeast-2.elasticbeanstalk.com/analysis/upload",
        {
          method: "POST",
          body: form,
        }
      );

      if (!res.ok) return alert("사진 업로드 실패");

      alert("금요일 사진 업로드 완료되었습니다.");
      setUploadedDays((prev) => ({ ...prev, [selectedDay]: true }));
      setPage("manage");
    } catch (err) {
      alert("서버 연결 오류");
    }
  };

  const handleGuestDistribute = async () => {
    try {
      const response = await fetch(
        "http://zongsul-env.eba-xmxykbwh.ap-northeast-2.elasticbeanstalk.com/distribution/active"
      );

      if (!response.ok) {
        alert("서버 오류");
        return;
      }

      const data = await response.json();

      const formatted = data.map((session) => {
        const slots = Array(session.capacity).fill(null);

        session.claims.forEach((claim, index) => {
  if (index < session.capacity) {
    slots[index] = {
      name: claim.name,
      studentId: claim.studentId,
      done: claim.done
    };
  }
});

        return { sessionId: session.id, name: session.menuName, slots };
      });

      setDistributedDishes(formatted);
    } catch (err) {
      console.error("서버 연결 실패:", err);
    }
  };

  // 금요일 분석 결과 가져오기
  useEffect(() => {
    if (page !== "analysisStart") return;

    const fetchResult = async () => {
      try {
        const res = await fetch(
          "http://zongsul-env.eba-xmxykbwh.ap-northeast-2.elasticbeanstalk.com/analysis/result"
        );

        if (!res.ok) {
          setFridayAnalysisResult({
            ratios: { 계란찜: 0.3, 김자반: 0.4, 시금치: 0.3 },
            leastPopular: "계란찜",
            related: ["두부조림"],
          });
          setAnalysisComplete(true);
          return;
        }

        const data = await res.json();
        setFridayAnalysisResult(data);
        setAnalysisComplete(true);
      } catch (err) {
        setFridayAnalysisResult({
          ratios: { 계란찜: 0.3, 김자반: 0.4, 시금치: 0.3 },
          leastPopular: "계란찜",
          related: ["두부조림"],
        });
        setAnalysisComplete(true);
      }
    };

    fetchResult();
  }, [page]);

  // 자동 로그인
  useEffect(() => {
    const savedName = localStorage.getItem("name");
    const savedStudentId = localStorage.getItem("studentId");

    if (savedName && savedStudentId) {
      setName(savedName);
      setStudentId(savedStudentId);
      alert(`${savedName}님, 자동 로그인되었습니다!`);
      setMode("guest");

      handleGuestDistribute();
    }
  }, []);

  // 손님용 잔반 현황 들어올 때마다 새로 가져오기
  useEffect(() => {
    if (page === "guestDistribution") handleGuestDistribute();
  }, [page]);

  const handleLogin = async () => {
    if (!name.trim() || !studentId.trim()) {
      alert("이름과 학번을 모두 입력하세요!");
      return;
    }
    try {
      const response = await fetch(
        "http://zongsul-env.eba-xmxykbwh.ap-northeast-2.elasticbeanstalk.com/api/auth/login",
        {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ name, studentId }),
        }
      );

      if (response.ok) {
        const data = await response.json();
        localStorage.setItem("name", data.name);
        localStorage.setItem("studentId", data.studentId);
        alert(`로그인 성공: ${data.name} (${data.studentId})`);
        setMode("guest");
        handleGuestDistribute();
      } else {
        alert("로그인 실패");
      }
    } catch (err) {
      console.error("서버 요청 실패:", err);
      alert("서버 연결 오류");
    }
  };

  const handleLogout = () => {
    localStorage.removeItem("name");
    localStorage.removeItem("studentId");
    setName("");
    setStudentId("");
    alert("로그아웃되었습니다.");
    setPage("home");
  };

  const AdminStatusBoard = ({ distributedDishes, setDistributedDishes }) => {
  const onClick = async (dishIndex, slotIndex) => {
    const slot = distributedDishes[dishIndex].slots[slotIndex];

    if (!slot) {
      return alert("아직 신청한 학생이 없습니다.");
    }

    const inputId = prompt("본인 확인을 위해 학번을 입력하세요:");
    if (!inputId) return;

    if (inputId !== slot.studentId) {
      return alert("학번이 일치하지 않습니다.");
    }

    await fetch(
      `http://zongsul-env.eba-xmxykbwh.ap-northeast-2.elasticbeanstalk.com/distribution/${distributedDishes[dishIndex].sessionId}/done`,
      {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          userName: slot.name,
          studentId: slot.studentId
        })
      }
    );

    setDistributedDishes((prev) => {
      const copy = [...prev];
      copy[dishIndex].slots[slotIndex] = {
        ...slot,
        done: true
      };
      return copy;
    });

    alert("배포 완료 처리되었습니다!");
  };

  return (
    <main className="main admin-main">
      {distributedDishes.map((dish, idx) => (
        <div key={idx} className="dish-board">
          <h3>{dish.name}</h3>
          <div className="slot-grid">
            {dish.slots.map((slot, j) => (
              <div
                key={j}
                onClick={() => onClick(idx, j)}
                className={`slot ${
                  slot?.done ? "filled" : slot ? "filled" : "empty"
                }`}
                style={{
                  backgroundColor: slot
                    ? slot?.done
                      ? "#b2bec3"
                      : "#ffeaa7"
                    : "#f1f2f6",
                }}
              >
                {slot?.done ? "완료" : slot?.name || "빈칸"}
              </div>
            ))}
          </div>
        </div>
      ))}
    </main>
  );
};



  const DistributionBoard = ({ editable }) => {
  const handleClick = async (dishIndex, slotIndex) => {
    const current = localStorage.getItem("name") || "이름없음";
    const studentId = localStorage.getItem("studentId");

    const target = distributedDishes[dishIndex];
    const slotObj = target.slots[slotIndex];
    const currentSlotName = slotObj ? slotObj.name : "";
    const sessionId = target.sessionId;

  if (slotObj?.done) {
    return alert("이미 배포 완료 처리된 반찬입니다.");
  }

    if (editable) {
      if (slotObj?.done) {
        return alert("완료된 반찬은 관리자도 수정할 수 없습니다.");
      }
      
      setDistributedDishes((prev) => {
        const copy = [...prev];
        copy[dishIndex].slots[slotIndex] = null;
        return copy;
      });
      return;
    }

    if (currentSlotName === current) {
      if (slotObj?.done) {
        return alert("완료된 반찬은 취소할 수 없습니다.");
      }

      try {
        const res = await fetch(
          `http://zongsul-env.eba-xmxykbwh.ap-northeast-2.elasticbeanstalk.com/distribution/${sessionId}/cancel`,
          {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ userName: current, studentId }),
          }
        );

        if (!res.ok) return alert("취소 실패 (서버 오류)");

        setDistributedDishes((prev) => {
          const copy = [...prev];
          copy[dishIndex].slots[slotIndex] = null;
          return copy;
        });

        await handleGuestDistribute();
      } catch (e) {
        alert("취소 요청 중 서버 오류");
      }
      return;
    }

    if (currentSlotName && currentSlotName !== current)
      return alert("이미 다른 사람이 선택한 칸입니다.");

    const alreadyTaken = target.slots.some(
      (s) => s && s.name === current
    );
    if (alreadyTaken) return alert("이 반찬은 이미 한 칸 선택했습니다.");

    const emptyIndex = target.slots.findIndex((s) => !s);
    if (emptyIndex === -1) return alert("이미 모두 신청 완료된 반찬입니다.");

    try {
      const res = await fetch(
        `http://zongsul-env.eba-xmxykbwh.ap-northeast-2.elasticbeanstalk.com/distribution/${sessionId}/claim`,
        {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ userName: current, studentId }),
        }
      );

      if (!res.ok) {
        let err = null;
        try {
          err = await res.json();
        } catch {}
        alert(`신청 실패: ${err?.message || res.status}`);
        return;
      }

      setDistributedDishes((prev) => {
        const copy = [...prev];
        copy[dishIndex].slots[emptyIndex] = {
          name: current,
          studentId: studentId,
        };
        return copy;
      });

      await handleGuestDistribute();
    } catch (e) {
      alert("서버 연결 오류");
    }
  };

  return (
    <main className="main">
      {distributedDishes.map((dish, i) => (
        <div key={i} className="dish-board">
          <h3>{dish.name}</h3>
          <div className="slot-grid">
            {dish.slots.map((slot, j) => (
              <div
                key={j}
                onClick={() => handleClick(i, j)}
                className={`slot ${slot ? "filled" : "empty"}`}
                style={{
                  backgroundColor: slot
                    ? editable
                      ? "#ff7675"
                      : "#74b9ff"
                    : "#f1f2f6",
                  cursor: "pointer",
                  opacity:
                    !editable &&
                    slot &&
                    slot.name !== localStorage.getItem("name")
                      ? 0.6
                      : 1,
                }}
              >
                {slot?.done ? "완료" : slot?.name || "빈칸"}
              </div>
            ))}
          </div>
        </div>
      ))}
    </main>
  );
};


  // ---------------- 페이지 분기 ----------------

  if (page === "manage") {
    const days = ["월", "화", "수", "목", "금"];

    const handleDayClick = (day) => {
      if (day !== "금") return;
      setSelectedDay(day);
      setPage("upload");
    };

    const startAnalysis = () => {
      const allUploaded = Object.values(uploadedDays).every(Boolean);
      if (!allUploaded) return alert("잔반 사진이 전부 들어오지 않았습니다.");

      setAnalysisComplete(false);
      setPage("analysisStart");
    };

    return (
      <div>
        <header className="header" onClick={() => setPage("home")}>
          잔반이들
        </header>

        <main className="main">
          <div className="week-container">
            <div className="week-bar">
              {days.map((day) => (
                <div
                  key={day}
                  className={`day-box ${day === "금" ? "active" : "disabled"}`}
                  onClick={() => handleDayClick(day)}
                  style={{
                    cursor: day === "금" ? "pointer" : "not-allowed",
                    opacity: day === "금" ? 1 : 0.5,
                    backgroundColor: day === "금" ? "#fff" : "#f5f5f5",
                  }}
                >
                  {day}
                </div>
              ))}
            </div>

            <button className="analyze-btn" onClick={startAnalysis}>
              잔반 분석 시작
            </button>
          </div>
        </main>

        <footer className="footer">
          <button onClick={() => setPage("home")}>손님용</button>
          <button onClick={() => setPage("home")}>관리자용</button>
        </footer>
      </div>
    );
  }

  if (page === "upload") {
    return (
      <div>
        <header className="header" onClick={() => setPage("manage")}>
          잔반이들: {selectedDay}요일
        </header>

        <main className="main-upload-container">
          <h2>{selectedDay}요일 사진 업로드</h2>
          <input ref={fileInputRef} type="file" multiple accept="image/*" />
          <p>여러 장의 사진을 선택할 수 있습니다.</p>
          <button onClick={handleUpload}>사진 업로드</button>
          <button className="back-btn" onClick={() => setPage("manage")}>
            뒤로가기
          </button>
        </main>

        <footer className="footer">
          <button onClick={() => setPage("home")}>손님용</button>
          <button onClick={() => setPage("home")}>관리자용</button>
        </footer>
      </div>
    );
  }

  if (page === "analysisStart") {
    return (
      <div>
        <header className="header">잔반 분석</header>
        <main className="main-upload-container">
          <h2>잔반 분석중...</h2>
          <p>금요일 사진을 기반으로 분석을 수행합니다.</p>

          <button
            disabled={!analysisComplete}
            onClick={() => setPage("analysisResults")}
          >
            다음
          </button>

          {!analysisComplete && (
            <p>분석 진행 중... 잠시만 기다려주세요.</p>
          )}
        </main>
      </div>
    );
  }

  if (page === "analysisResults") {
    const combined = {
      월: mockResults.월,
      화: mockResults.화,
      수: mockResults.수,
      목: mockResults.목,
      금: fridayAnalysisResult || {
        ratios: {},
        leastPopular: "-",
        related: [],
      },
    };

    return (
      <div>
        <header className="header">분석 결과</header>

        <main className="analysis-results-container">
          <div className="analysis-grid">
            {Object.entries(combined).map(([day, data]) => (
              <div key={day} className="analysis-card">
                <h3>{day}요일</h3>

                <div className="analysis-section">
                  <div className="analysis-section-title">비율</div>
                  <ul>
                    {data.ratios &&
                      Object.entries(data.ratios).map(([k, v]) => (
                        <li key={k}>
                          {k}: {(v * 100).toFixed(1)}%
                        </li>
                      ))}
                  </ul>
                </div>

                <div className="analysis-section">
                  <div className="analysis-section-title">
                    가장 인기없는 반찬
                  </div>
                  <p>{data.leastPopular}</p>
                </div>

                <div className="analysis-section">
                  <div className="analysis-section-title">관련 서브반찬</div>
                  <p>{(data.related || []).join(", ")}</p>
                </div>
              </div>
            ))}
          </div>

          <button
            className="analysis-next-btn"
            onClick={() => setPage("analysisMenuPlan")}
          >
            다음
          </button>
        </main>
      </div>
    );
  }

  if (page === "finalMenuPlan") {
    const weeklyLeast = {
      월: "미역줄기볶음",
      화: "김치전",
      수: "도라지무침",
      목: "오이무침",
      금: fridayAnalysisResult?.leastPopular ?? "-",
    };

    const replacementMap = {
      미역줄기볶음: "고사리나물",
      김치전: "군만두",
      도라지무침: "애호박볶음",
      오이무침: "무생채",
      계란찜: "두부조림",
      김자반: "멸치볶음",
      시금치: "브로콜리",
    };

    const riceDB = ["흰쌀밥", "잡곡밥", "흑미밥"];
    const soupDB = ["된장국", "미역국", "소고기무국"];
    const mainDB = ["제육볶음", "고등어구이", "불고기", "닭갈비"];

    const subDB = [
      "계란찜",
      "김치전",
      "시금치",
      "어묵볶음",
      "두부조림",
      "멸치볶음",
      "브로콜리",
    ];

    const namulDB = [
      "콩나물무침",
      "고사리나물",
      "미역줄기볶음",
      "오이무침",
      "도라지무침",
      "애호박볶음",
      "무생채",
    ];

    const generateFinalMenu = () => {
      const days = ["월", "화", "수", "목", "금"];
      const result = [];

      days.forEach((day) => {
        const least = weeklyLeast[day];
        const substitute = replacementMap[least] ?? "(대체 없음)";

        const rice = riceDB[Math.floor(Math.random() * riceDB.length)];
        const soup = soupDB[Math.floor(Math.random() * soupDB.length)];
        const main = mainDB[Math.floor(Math.random() * mainDB.length)];

        let subSide = "";
        let namul = "";

        if (subDB.includes(least)) {
          subSide = substitute;
          namul = namulDB[Math.floor(Math.random() * namulDB.length)];
        } else if (namulDB.includes(least)) {
          namul = substitute;
          subSide = subDB[Math.floor(Math.random() * subDB.length)];
        } else {
          subSide = substitute;
          namul = namulDB[Math.floor(Math.random() * namulDB.length)];
        }

        result.push({
          day,
          rice,
          soup,
          main,
          sub: subSide,
          namul,
          replacedFrom: least,
          replacedTo: substitute,
        });
      });

      return result;
    };

    const finalPlan = generateFinalMenu();

    return (
      <div>
        <header className="header">최종 식단표</header>

        <main className="analysis-results-container">
          <h2 style={{ textAlign: "center", marginBottom: "20px" }}>
            대체 반영 완료된 최종 식단표
          </h2>

          <div className="analysis-grid">
            {finalPlan.map((p) => (
              <div key={p.day} className="analysis-card">
                <h3>{p.day}요일</h3>
                <p>
                  <strong>밥</strong>
                  <br />
                  {p.rice}
                </p>
                <p>
                  <strong>국</strong>
                  <br />
                  {p.soup}
                </p>
                <p>
                  <strong>메인</strong>
                  <br />
                  {p.main}
                </p>
                <p>
                  <strong>서브 반찬</strong>
                  <br />
                  {p.sub}
                </p>
                <p>
                  <strong>나물</strong>
                  <br />
                  {p.namul}
                </p>

                <p
                  style={{
                    marginTop: "12px",
                    fontSize: "14px",
                    color: "#444",
                  }}
                >
                  <strong>대체 반찬</strong>
                  <br />
                  {p.replacedFrom} → {p.replacedTo}
                </p>
              </div>
            ))}
          </div>

          <div style={{ textAlign: "center", marginTop: "30px" }}>
            <button className="menu-wide-btn" onClick={() => setPage("home")}>
              종료
            </button>
          </div>
        </main>
      </div>
    );
  }

  if (page === "analysisMenuPlan") {
    const fridayLeast = fridayAnalysisResult?.leastPopular;
    const weeklyLeast = {
      월: "미역줄기볶음",
      화: "김치전",
      수: "도라지무침",
      목: "오이무침",
      금:
        fridayLeast && typeof fridayLeast === "string"
          ? fridayLeast
          : "계란찜",
    };

    const replacementMap = {
      미역줄기볶음: "고사리나물",
      김치전: "군만두",
      도라지무침: "애호박볶음",
      오이무침: "무생채",
      계란찜: "두부조림",
      김자반: "멸치볶음",
      시금치: "브로콜리",
    };

    const riceDB = ["흰쌀밥", "잡곡밥", "흑미밥"];
    const soupDB = ["된장국", "미역국", "소고기무국"];
    const mainDB = ["제육볶음", "고등어구이", "불고기", "닭갈비"];

    const subDB = [
      "계란찜",
      "김치전",
      "시금치",
      "어묵볶음",
      "두부조림",
      "멸치볶음",
      "브로콜리",
    ];

    const namulDB = [
      "콩나물무침",
      "고사리나물",
      "미역줄기볶음",
      "오이무침",
      "도라지무침",
      "애호박볶음",
      "무생채",
    ];

    const generateMenuPlan = () => {
      const days = ["월", "화", "수", "목", "금"];
      const result = [];

      days.forEach((day) => {
        const least = weeklyLeast[day];
        const substitute = replacementMap[least] || "(대체 없음)";

        const rice = riceDB[Math.floor(Math.random() * riceDB.length)];
        const soup = soupDB[Math.floor(Math.random() * soupDB.length)];
        const main = mainDB[Math.floor(Math.random() * mainDB.length)];

        let subSide = "";
        let namul = "";

        if (subDB.includes(least)) {
          subSide = least;
          namul = namulDB[Math.floor(Math.random() * namulDB.length)];
        } else if (namulDB.includes(least)) {
          namul = least;
          subSide = subDB[Math.floor(Math.random() * subDB.length)];
        } else {
          subSide = subDB[Math.floor(Math.random() * subDB.length)];
          namul = namulDB[Math.floor(Math.random() * namulDB.length)];
        }

        result.push({
          day,
          rice,
          soup,
          main,
          sub: subSide,
          namul,
          replacedFrom: least,
          replacedTo: substitute,
        });
      });

      return result;
    };

    const plan = generateMenuPlan();

    const saveAsImage = async () => {
      if (!window.html2canvas) {
        const script = document.createElement("script");
        script.src = "https://html2canvas.hertzen.com/dist/html2canvas.min.js";
        document.body.appendChild(script);
        await new Promise((res) => (script.onload = res));
      }

      if (window.html2canvas && captureRef.current) {
        const canvas = await window.html2canvas(captureRef.current);
        const a = document.createElement("a");
        a.href = canvas.toDataURL("image/png");
        a.download = "menu_plan.png";
        a.click();
      }
    };

    return (
      <div>
        <header className="header">다음주 식단표 제안</header>

        <main className="analysis-results-container">
          <div ref={captureRef}>
            <h2 style={{ textAlign: "center", marginBottom: "30px" }}>
              5일치 식단표
            </h2>

            <div className="analysis-grid">
              {plan.map((p) => (
                <div key={p.day} className="analysis-card">
                  <h3 style={{ marginBottom: "10px" }}>{p.day}요일</h3>

                  <p>
                    <strong>밥</strong>
                    <br />
                    {p.rice}
                  </p>
                  <p>
                    <strong>국</strong>
                    <br />
                    {p.soup}
                  </p>
                  <p>
                    <strong>메인</strong>
                    <br />
                    {p.main}
                  </p>
                  <p>
                    <strong>서브 반찬</strong>
                    <br />
                    {p.sub}
                  </p>
                  <p>
                    <strong>나물</strong>
                    <br />
                    {p.namul}
                  </p>

                  <p
                    style={{
                      marginTop: "12px",
                      fontSize: "14px",
                      color: "#444",
                      borderTop: "1px dashed #ccc",
                      paddingTop: "10px",
                    }}
                  >
                    <strong>대체 정보</strong>
                    <br />
                    {p.replacedFrom} → {p.replacedTo}
                  </p>
                </div>
              ))}
            </div>
          </div>

          <div
            style={{
              display: "flex",
              flexDirection: "column",
              alignItems: "center",
              gap: "12px",
              marginTop: "40px",
              width: "100%",
            }}
          >
            <button className="menu-wide-btn" onClick={saveAsImage}>
              사진으로 저장
            </button>

            <button
              className="menu-wide-btn"
              onClick={() => setPage("finalMenuPlan")}
            >
              최종 식단표 보기
            </button>

            <button
              className="menu-wide-btn"
              onClick={() => setPage("home")}
            >
              종료
            </button>
          </div>
        </main>
      </div>
    );
  }

  if (page === "adminDistribution") {
    return (
      <div>
        <header className="header" onClick={() => setPage("home")}>
          🍱 관리자 잔반 배포
        </header>
        <DistributionBoard editable={true} />
        <footer className="footer">
          <button onClick={() => setPage("home")}>홈으로</button>
        </footer>
      </div>
    );
  }

  if (page === "adminStatus") {
    return (
      <div>
        <header className="header" onClick={() => setPage("home")}>
          🧑‍🍳 관리자용 잔반 관리 현황
        </header>

        <AdminStatusBoard
          distributedDishes={distributedDishes}
          setDistributedDishes={setDistributedDishes}
        />

        <footer className="footer">
          <button onClick={() => setPage("home")}>홈으로</button>
        </footer>
      </div>
    );
  }

  if (page === "guestDistribution") {
    return (
      <div>
        <header className="header" onClick={() => setPage("home")}>
          🍛 손님용 잔반 현황
        </header>
        <DistributionBoard editable={false} />

        <footer className="footer">
          <button onClick={() => setPage("home")}>홈으로</button>
          {localStorage.getItem("name") && (
            <button
              onClick={handleLogout}
              style={{
                marginLeft: "10px",
                backgroundColor: "#d9534f",
                color: "white",
              }}
            >
              로그아웃
            </button>
          )}
        </footer>
      </div>
    );
  }

  if (page === "distribute") {
    const handleDishChange = (index, field, value) => {
      const newDishes = [...dishes];
      newDishes[index][field] = value;
      setDishes(newDishes);
    };

    const handleSubmit = async () => {
      const filtered = dishes.filter((d) => d.name && d.count);
      if (filtered.length === 0) return alert("반찬 정보를 입력하세요!");

      try {
        const payload = filtered.map((dish) => ({
          menuName: dish.name,
          capacity: Number(dish.count),
        }));

        const res = await fetch(
          "http://zongsul-env.eba-xmxykbwh.ap-northeast-2.elasticbeanstalk.com/distribution/batch",
          {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload),
          }
        );

        if (!res.ok) return alert("반찬 등록 실패");

        const sessions = await res.json();

        const formatted = sessions.map((s) => ({
          sessionId: s.id,
          name: s.menuName,
          slots: Array(s.capacity).fill(""),
        }));

        setDistributedDishes(formatted);
        alert("잔반 배포가 시작되었습니다!");
        setPage("distributionBoard");
      } catch {
        alert("서버 연결 오류");
      }
    };

    return (
      <div>
        <header className="header" onClick={() => setPage("home")}>
          잔반 배포
        </header>

        <main className="main-upload-container">
          <h2>반찬 정보 입력 (최대 4개)</h2>
          {dishes.map((dish, idx) => (
            <div key={idx} style={{ marginBottom: "15px", width: "100%" }}>
              <input
                type="text"
                placeholder={`반찬 ${idx + 1} 이름`}
                value={dish.name}
                onChange={(e) => handleDishChange(idx, "name", e.target.value)}
                style={{ marginBottom: "8px" }}
              />
              <input
                type="number"
                placeholder={`반찬 ${idx + 1} 개수`}
                value={dish.count}
                onChange={(e) => handleDishChange(idx, "count", e.target.value)}
              />
            </div>
          ))}
          <button onClick={handleSubmit}>배포 시작</button>
          <button className="back-btn" onClick={() => setPage("home")}>
            뒤로가기
          </button>
        </main>
      </div>
    );
  }

  // 기본 홈 (손님 / 관리자 선택)
  return (
    <div>
      <header className="header" onClick={() => setMode("guest")}>
        잔반이들
      </header>

      <main className="main">
        {mode === "guest" ? (
          <div className="login-box">
            <h2>손님 로그인</h2>

            <input
              type="text"
              placeholder="이름을 입력하세요"
              value={name}
              onChange={(e) => setName(e.target.value)}
              readOnly={!!localStorage.getItem("name")}
              style={{
                backgroundColor: localStorage.getItem("name") ? "#eee" : "white",
                cursor: localStorage.getItem("name") ? "not-allowed" : "text",
              }}
            />

            <input
              type="text"
              placeholder="학번을 입력하세요"
              value={studentId}
              onChange={(e) => setStudentId(e.target.value)}
              readOnly={!!localStorage.getItem("name")}
              style={{
                backgroundColor: localStorage.getItem("name") ? "#eee" : "white",
                cursor: localStorage.getItem("name") ? "not-allowed" : "text",
              }}
            />


            {!localStorage.getItem("name") && (
              <button onClick={handleLogin}>로그인</button>
            )}

            {localStorage.getItem("name") && (
              <>
                <button
                  style={{ marginTop: "10px" }}
                  onClick={() => {
                    handleGuestDistribute();
                    setPage("guestDistribution");
                  }}
                >
                  잔반 배포 현황 보기
                </button>

                <button
                  style={{
                    marginTop: "10px",
                    backgroundColor: "#d9534f",
                    color: "white",
                  }}
                  onClick={handleLogout}
                >
                  로그아웃
                </button>
              </>
            )}
          </div>
        ) : (
          <div className="admin-box">
            <button onClick={() => setPage("manage")}>잔반 관리 시작</button>
            <button onClick={() => setPage("distribute")}>
              잔반 배포 시작 (입력)
            </button>
            <button onClick={() => setPage("adminStatus")}>
              잔반 관리 현황
            </button>
          </div>
        )}
      </main>

      <footer className="footer">
        <button
          className={mode === "guest" ? "active" : ""}
          onClick={() => {
            setMode("guest");
            setPage("home");
          }}
        >
          손님용
        </button>

        <button
          className={mode === "admin" ? "active" : ""}
          onClick={() => {
            setMode("admin");
            setPage("home");
          }}
        >
          관리자용
        </button>
      </footer>
    </div>
  );
}

export default App;
