import { useState, useEffect } from "react";
import "./App.css";

function App() {
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

  const handleGuestDistribute = async () => {
  try {
    const response = await fetch("http://192.168.0.69:8080/distribution/active");
    if (response.ok) {
      const data = await response.json();

      const formatted = data.map(session => ({
        sessionId: session.id,
        name: session.menuName,
        slots: Array(session.capacity).fill("")
      }));

      setDistributedDishes(formatted);
    }
  } catch (err) {
    console.error("서버 연결 실패:", err);
  }

  setPage("guestDistribution");
};


  useEffect(() => {
    const savedName = localStorage.getItem("name");
    const savedStudentId = localStorage.getItem("studentId");
    if (savedName && savedStudentId) {
      setName(savedName);
      setStudentId(savedStudentId);
      alert(`${savedName}님, 자동 로그인되었습니다!`);
      setMode("guest");
      // 자동 로그인 성공 시 바로 잔반 현황을 가져와 페이지로 이동
      handleGuestDistribute();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleLogin = async () => {
    if (!name.trim() || !studentId.trim()) {
      alert("이름과 학번을 모두 입력하세요!");
      return;
    }
    try {
      const response = await fetch("http://192.168.0.69:8080/api/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ name, studentId }),
      });
      if (response.ok) {
        const data = await response.json();
        localStorage.setItem("name", data.name);
        localStorage.setItem("studentId", data.studentId);
        alert(`로그인 성공: ${data.name} (${data.studentId})`);
        setMode("guest");
        // 로그인 성공 시 잔반 현황을 가져와 페이지로 이동
        handleGuestDistribute();
      } else {
        alert("로그인 실패");
      }
    } catch (error) {
      console.error("서버 요청 실패:", error);
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
    setMode("guest");
  };

  // 관리자 배포는 그대로 유지
  const handleAdminDistribute = () => {
    const filtered = dishes.filter((d) => d.name && d.count);
    const formatted = filtered.map((d) => ({
      name: d.name,
      slots: Array(Number(d.count)).fill(""),
    }));
    setDistributedDishes(formatted);
    setPage("distribute");
  };

  const DistributionBoard = ({ editable }) => {
    const handleClick = (dishIndex, slotIndex) => {
  const current = localStorage.getItem("name") || "이름없음";
  const studentId = localStorage.getItem("studentId");

  // 1) 먼저 화면 업데이트 로직은 순수하게 (동기)
  setDistributedDishes((prev) => {
    const newArr = [...prev];
    const target = newArr[dishIndex];
    const currentName = target.slots[slotIndex];

    if (editable) {
      target.slots[slotIndex] = "";
      return newArr;
    }

    if (currentName === current) {
      target.slots[slotIndex] = "";
      return newArr;
    }

    if (currentName && currentName !== current) {
      alert("이미 다른 사람이 선택한 칸입니다.");
      return prev;
    }

    // 서버 요청은 바깥에서 처리하므로 여기에서는 화면만 잠시 업데이트 x
    return prev;
  });

  // 2) 서버 통신은 setState 밖에서 비동기로 처리
  const target = distributedDishes[dishIndex];
  const sessionId = target.sessionId;

  fetch(`http://192.168.0.69:8080/distribution/${sessionId}/claim`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      userName: current,
      studentId: studentId,
    }),
  })
    .then(async (res) => {
      if (!res.ok) {
        const err = await res.json().catch(() => null);
        alert(`신청 실패: ${err?.message || res.status}`);
        return;
      }

      // 서버 성공 → 이제 화면 업데이트
      setDistributedDishes((prev) => {
        const newArr = [...prev];
        newArr[dishIndex].slots[slotIndex] = current;
        return newArr;
      });
    })
    .catch(() => {
      alert("서버 연결 오류");
    });
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
                    !editable && slot && slot !== localStorage.getItem("name")
                      ? 0.6
                      : 1,
                }}
              >
                {slot || "빈칸"}
              </div>
            ))}
          </div>
        </div>
      ))}
    </main>
  );
};
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

  // 손님용 잔반 현황 페이지
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

  // 사진 업로드 화면
  if (page === "upload") {
    return (
      <div>
        <header className="header" onClick={() => setPage("manage")}>
          잔반이들: {selectedDay}요일
        </header>

        <main className="main-upload-container">
          <h2>{selectedDay}요일 사진 업로드</h2>
          <input type="file" multiple accept="image/*" />
          <p>여러 장의 사진을 선택할 수 있습니다.</p>
          <button className="back-btn" onClick={() => setPage("manage")}>
            뒤로가기
          </button>
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

  // 관리자용: 요일 선택 및 잔반 배포 시작
  if (page === "manage") {
    return (
      <div>
        <header
          className="header"
          onClick={() => {
            setPage("home");
            setMode("guest");
          }}
        >
          잔반이들
        </header>

        <main className="main">
          <div className="week-container">
            <div className="week-bar">
              {["월", "화", "수", "목", "금"].map((day) => (
                <div
                  key={day}
                  className="day-box"
                  onClick={() => {
                    setSelectedDay(day);
                    setPage("upload");
                  }}
                >
                  {day}
                </div>
              ))}
            </div>
            {/* 이 버튼은 distribute 페이지로 이동합니다. */}
            <button className="analyze-btn" onClick={() => setPage("distribute")}>
              잔반 배포 시작
            </button>
          </div>
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

  // 잔반 배포 화면 (관리자용)
  if (page === "distribute") {
    const handleDishChange = (index, field, value) => {
      const newDishes = [...dishes];
      newDishes[index][field] = value;
      setDishes(newDishes);
    };
    
    const handleSubmit = async () => {
      // ✅ 입력값 검증

      const filtered = dishes.filter((d) => d.name && d.count);
      if (filtered.length === 0) {
        alert("반찬 정보를 입력하세요!");
        return;
      }

      filtered.forEach(dish => {
        console.log("보내는 데이터:", dish.name, dish.count);
     });

      try {
        // ✅ 각 반찬 정보를 백엔드로 전송
        for (const dish of filtered) {
          const res = await fetch("http://192.168.0.69:8080/distribution/start", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
              menuName: dish.name,
              capacity: Number(dish.count),
            }),
          });

          if (!res.ok) {
            console.error(`❌ ${dish.name} 등록 실패`);
          } else {
            const data = await res.json();
            console.log(`✅ ${data.menuName} (${data.capacity}) 등록 완료`);
          }
        }

        // ✅ (프론트단 시각화용) 잔반판 생성
        const formatted = filtered.map((d) => ({
          name: d.name,
          slots: Array(Number(d.count)).fill(""),
        }));
        setDistributedDishes(formatted);

        alert("✅ 잔반 배포가 시작되었습니다!");
        setPage("distributionBoard");
      } catch (err) {
        console.error("서버 연결 오류:", err);
        alert("서버 연결 오류가 발생했습니다.");
      }
    };

    return (
      <div>
        <header className="header" onClick={() => setPage("manage")}>
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
          <button
            className="back-btn"
            onClick={() => setPage("home")}
            style={{ marginTop: "10px" }}
          >
            뒤로가기
          </button>
        </main>
      </div>
    );
  }

  // 관리자/손님 공통: 배포된 잔반판 화면 (distribute 페이지에서 배포 시작 시 사용)
  if (page === "distributionBoard") {
    const handleClick = (dishIndex, slotIndex) => {
      const current = localStorage.getItem("name") || "이름없음";
      setDistributedDishes((prev) => {
        const newArr = [...prev];
        const target = newArr[dishIndex];
        const currentName = target.slots[slotIndex];
        if (mode === "admin") {
          // 관리자: 클릭하면 비우기
          target.slots[slotIndex] = "";
        } else {
          // 손님: 클릭하면 내 이름 토글
          if (currentName === current) {
            target.slots[slotIndex] = ""; // 이미 내가 선택했으면 취소
          } else if (!currentName) {
            target.slots[slotIndex] = current; // 빈칸이면 내가 선택
          }
        }
        return newArr;
      });
    };

    return (
      <div>
        <header className="header" onClick={() => setPage("home")}>
          잔반 배포판
        </header>

        <main className="main">
          {distributedDishes.map((dish, i) => (
            <div key={i} style={{ marginBottom: "25px" }}>
              <h3>{dish.name}</h3>
              <div
                style={{ display: "flex", flexWrap: "wrap", gap: "10px" }}
              >
                {dish.slots.map((slot, j) => (
                  <div
                    key={j}
                    onClick={() => handleClick(i, j)}
                    style={{
                      width: "100px",
                      height: "100px",
                      border: "2px solid gray",
                      borderRadius: "10px",
                      display: "flex",
                      alignItems: "center",
                      justifyContent: "center",
                      backgroundColor: slot ? "#ffcc80" : "#f0f0f0",
                      cursor: "pointer",
                    }}
                  >
                    {slot || "빈칸"}
                  </div>
                ))}
              </div>
            </div>
          ))}
        </main>

        <footer className="footer">
          <button onClick={() => setPage("home")}>홈으로</button>
        </footer>
      </div>
    );
  }

  // 기본 홈 화면
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
            />
            <input
              type="text"
              placeholder="학번을 입력하세요"
              value={studentId}
              onChange={(e) => setStudentId(e.target.value)}
            />
            <button onClick={handleLogin}>로그인</button>

            {/* 잔반 현황 바로 가기 버튼 추가 (로그인 상태일 때만) */}
            {localStorage.getItem("name") && (
              <>
                <button
                  style={{ marginTop: "10px" }}
                  onClick={handleGuestDistribute} // 잔반 현황 가져오기 및 페이지 이동
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